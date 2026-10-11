package com.careerlens.backend.service;

import com.careerlens.backend.model.Notification;
import com.careerlens.backend.model.NotificationType;
import com.careerlens.backend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification("notif1", "user1", NotificationType.APPLICATION_SUBMITTED,
                "Test Title", "Test Message", "entity1", false, Instant.now());
    }

    @Test
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification created = notificationService.createNotification("user1", NotificationType.APPLICATION_SUBMITTED,
                "Test Title", "Test Message", "entity1");

        assertNotNull(created);
        assertEquals("user1", created.getUserId());
        assertEquals("Test Title", created.getTitle());
        assertFalse(created.isRead());
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndReadFalse("user1")).thenReturn(3L);

        long count = notificationService.getUnreadCount("user1");
        assertEquals(3L, count);
    }

    @Test
    void testMarkAsRead() {
        when(notificationRepository.findByIdAndUserId("notif1", "user1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification updated = notificationService.markAsRead("notif1", "user1");

        assertTrue(updated.isRead());
    }

    @Test
    void testMarkAllAsRead() {
        when(notificationRepository.findByUserIdAndReadFalse("user1")).thenReturn(List.of(notification));

        notificationService.markAllAsRead("user1");

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).saveAll(any());
    }
}
