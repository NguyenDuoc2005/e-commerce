package com.ecommerce.seller.service;

import com.ecommerce.seller.client.CatalogClient;
import com.ecommerce.seller.client.NotificationClient;
import com.ecommerce.seller.client.OrderClient;
import com.ecommerce.seller.client.UserClient;
import com.ecommerce.seller.entity.Review;
import com.ecommerce.seller.repository.ReviewRepository;
import com.ecommerce.seller.repository.SellerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewModerationTest {
    @Mock ReviewRepository repository;
    @Mock OrderClient orderClient;
    @Mock CatalogClient catalogClient;
    @Mock SellerRepository sellerRepository;
    @Mock UserClient userClient;
    @Mock NotificationClient notificationClient;

    @Test
    void hidingReviewRemovesItFromRatingAggregate() {
        Review review = new Review(); review.setProductId("product-1"); review.setStatus("VISIBLE");
        when(repository.findById("review-1")).thenReturn(Optional.of(review));
        when(repository.averageProductRating("product-1")).thenReturn(4.5D);
        when(repository.countByProductIdAndStatus("product-1", "VISIBLE")).thenReturn(8L);
        ReviewService service = new ReviewService(repository, orderClient, catalogClient, new ObjectMapper(), sellerRepository, userClient, notificationClient);

        service.hideByAdmin("review-1");

        assertEquals("HIDDEN", review.getStatus());
        verify(repository).save(review);
        verify(catalogClient).updateRating("product-1", 4.5D, 8L);
    }
}
