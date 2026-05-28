package com.project.code.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Name cannot be null")
	private String name;

	@NotNull(message = "Category cannot be null")
	private String category;

	@NotNull(message = "Price cannot be null")
	private Double price;

	@NotNull(message = "SKU cannot be null")
	private String sku;

	@OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonManagedReference("inventory-product")
	private Set<Inventory> inventories;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public Double getPrice() { return price; }
	public void setPrice(Double price) { this.price = price; }

	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }

	public Set<Inventory> getInventories() { return inventories; }
	public void setInventories(Set<Inventory> inventories) { this.inventories = inventories; }
}


