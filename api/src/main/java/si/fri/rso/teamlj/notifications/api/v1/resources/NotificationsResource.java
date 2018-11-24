package si.fri.rso.teamlj.notifications.api.v1.resources;

import si.fri.rso.teamlj.notifications.entities.Notification;
import si.fri.rso.teamlj.notifications.services.beans.NotificationsBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationsResource {

    @Context
    private UriInfo uriInfo;

    @Inject
    private NotificationsBean notificationsBean;

    @GET
    public Response getNotifications() {

        List<Notification> notifications = notificationsBean.getNotifications();

        return Response.ok(notifications).build();
    }

    @GET
    @Path("/{notificationId}")
    public Response getNotification(@PathParam("notificationId") Integer notificationId) {

        Notification notifications = notificationsBean.getNotification(notificationId);

        if (notifications == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(notifications).build();
    }

    @POST
    public Response createNotification(Notification notification) {

        notification = notificationsBean.createNotification(notification);

        if (notification.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(notification).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(notification).build();
        }
    }

    @PUT
    @Path("/{notificationId}")
    public Response putNotification(@PathParam("notificationId") Integer notificationId, Notification notification) {

        notification = notificationsBean.putNotification(notificationId, notification);

        if (notification == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (notification.getId() != null)
                return Response.status(Response.Status.OK).entity(notification).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("/{notificationId}")
    public Response deleteNotification(@PathParam("notificationId") Integer notificationId) {

        boolean deleted = notificationsBean.deleteNotification(notificationId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
