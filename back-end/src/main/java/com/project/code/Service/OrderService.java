package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        // 1. get or create customer
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        // 2. fetch store
        Optional<Store> storeOpt = storeRepository.findById(placeOrderRequest.getStoreId());
        if (storeOpt.isEmpty()) {
            throw new IllegalArgumentException("Store not found for id: " + placeOrderRequest.getStoreId());
        }
        Store store = storeOpt.get();

        // 3. create orderDetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());
        orderDetails = orderDetailsRepository.save(orderDetails);

        // 4. create order items and update inventory
        ArrayList<OrderItem> createdItems = new ArrayList<>();
        for (PurchaseProductDTO pp : placeOrderRequest.getPurchaseProduct()) {
            Inventory inv = inventoryRepository.findByProductIdandStoreId(pp.getId(), store.getId());
            if (inv == null) {
                throw new IllegalStateException("No inventory record for product " + pp.getId() + " at store " + store.getId());
            }
            if (inv.getStockLevel() < pp.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product " + pp.getId());
            }
            inv.setStockLevel(inv.getStockLevel() - pp.getQuantity());
            inventoryRepository.save(inv);

            Product p = productRepository.findById(pp.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + pp.getId()));

            OrderItem item = new OrderItem();
            item.setOrder(orderDetails);
            item.setProduct(p);
            item.setQuantity(pp.getQuantity());
            item.setPrice(pp.getPrice());
            item = orderItemRepository.save(item);

            createdItems.add(item);
        }

        orderDetails.setOrderItems(createdItems);
        orderDetailsRepository.save(orderDetails);
    }
}
