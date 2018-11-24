package si.fri.rso.teamlj.notifications.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import si.fri.rso.teamlj.notifications.entities.Notification;
import si.fri.rso.teamlj.notifications.services.configuration.AppProperties;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ManagedBean
@ApplicationScoped
@Named
public class NotificationsBean {

    private Logger log = Logger.getLogger(NotificationsBean.class.getName());

    private Client httpClient;

    //private String baseUrl;

    @Inject
    @DiscoverService("rso-notifications")
    private Optional<String> baseUrl;

    @Inject
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        //baseUrl = "http://localhost:8085"; // notifications
    }

    public List<Notification> getNotifications() {

        TypedQuery<Notification> query = em.createNamedQuery("Notification.getAll", Notification.class);

        return query.getResultList();

    }

    public Notification getNotification(Integer notificationsId) {

        Notification notifications = em.find(Notification.class, notificationsId);

        if (notifications == null) {
            log.warning("notifications does not exist/notifications was deleted");
            throw new NotFoundException();
        }

        return notifications;
    }

    public Notification createNotification(Notification notification) {

        try {
            beginTx();
            em.persist(notification);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return notification;
    }

    public Notification putNotification(Integer notificationsId, Notification notification) {

        Notification notifications = em.find(Notification.class, notificationsId);

        if (notifications == null) {
            return null;
        }

        try {
            beginTx();
            notification.setId(notifications.getId());
            notification = em.merge(notification);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return notification;
    }

    public boolean deleteNotification(Integer notificationsId) {

        Notification notification = em.find(Notification.class, notificationsId);

        if (notification != null) {
            try {
                beginTx();
                em.remove(notification);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}
