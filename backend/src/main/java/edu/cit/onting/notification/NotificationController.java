package edu.cit.onting.notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationEventListener notificationEventListener;

    public NotificationController(NotificationEventListener notificationEventListener) {
        this.notificationEventListener = notificationEventListener;
    }

    @GetMapping
    public List<Notification> getNotifications() {
        return notificationEventListener.getAllNotifications();
    }
}