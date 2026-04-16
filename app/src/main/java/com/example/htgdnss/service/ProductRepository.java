package com.example.htgdnss.service;

import com.example.htgdnss.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProductRepository {

    private static ProductRepository instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION = "products";

    private ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    public Task<QuerySnapshot> getDanhSach() {
        return db.collection(COLLECTION).get();
    }

    public void themSanPham(Product product, OnCompleteListener<Void> listener) {
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(db.collection(COLLECTION).document().getId());
        }
        product.setCreatedAt(System.currentTimeMillis());
        product.setUpdatedAt(System.currentTimeMillis());

        db.collection(COLLECTION)
                .document(product.getProductId())
                .set(product)
                .addOnCompleteListener(listener);
    }

    public Task<Void> xoaSanPham(String productId) {
        return db.collection(COLLECTION).document(productId).delete();
    }
}