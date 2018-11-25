package si.fri.rso.teamlj.notifications.api.v1.resources;

import si.fri.rso.teamlj.notifications.dtos.User;
import si.fri.rso.teamlj.notifications.entities.Notification;
import si.fri.rso.teamlj.notifications.services.beans.NotificationsBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@RequestScoped
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationsResource {

    @Context
    private UriInfo uriInfo;

    @Inject
    private NotificationsBean notificationsBean;

    //TEST CALL
    @GET
    public Response getNotifications() {

        Notification notification = new Notification("test", 0, 0, 0, new User());

        return Response.ok(notification).build();
    }


    @GET
    @Path("/{userId}/{lan}&{lon}")
    public Response getNotification(@PathParam("userId") Integer userId, @PathParam("lan") Float latitude, @PathParam("lon") Float longitude) {

        Notification notifications = notificationsBean.getNotification(userId, latitude, longitude);

        if (notifications == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(notifications).build();
    }

}
