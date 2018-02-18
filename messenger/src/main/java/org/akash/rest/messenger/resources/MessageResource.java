package org.akash.rest.messenger.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.akash.rest.messenger.model.Message;
import org.akash.rest.messenger.resources.beans.MessageFilterBean;
import org.akash.rest.messenger.service.MessageService;


@Path("messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(value = {MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
public class MessageResource {
	
	MessageService mr = new MessageService();
	
	@GET
	public List<Message> getMessages(@BeanParam MessageFilterBean filterBean){
		if(filterBean.getYear() > 0){
			return mr.getAllMessagesForYear(filterBean.getYear());
		}
		if(filterBean.getStart() > 0 && filterBean.getSize() > 0){
			return mr.getAllMessagesPaginated(filterBean.getStart(), filterBean.getSize());
		}
		return mr.getAllMessages();
	}
	
	@POST
	public Response addMessage(Message message, @Context UriInfo uriInfo){
		Message nm = mr.addMessage(message);
		URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(message.getId())).build();
		return Response.created(uri)
				.entity(nm)
				.build();
		
	}
	
	@PUT
	@Path("/{messageId}")
	public Message updateMessage(@PathParam("messageId") long messageId, Message message){
		message.setId(messageId);
		return mr.updateMessage(message);
	}
	
	@DELETE
	@Path("/{messageId}")
	public Message deleteMessage(@PathParam("messageId") long id){
		return mr.removeMessage(id);
	}
	
	@GET
	@Path("/{messageId}")
	public Message getMessage(@PathParam("messageId") long messageId, @Context UriInfo uriInfo){
		Message message =  mr.getMessage(messageId);
		message.addLink(getUriForSelf(messageId, uriInfo), "self");
		message.addLink(getUriForProfile(message, uriInfo), "profile");
		message.addLink(getUriForComments(message, uriInfo), "comments");
		return message;
	}	
	
	
	@Path("/{messageId}/comments")
	public CommentResource getCommentResource(){
		return new CommentResource();
	}
	
	private String getUriForSelf(long messageId, UriInfo uriInfo){
		return  uriInfo.getBaseUriBuilder()
				.path(MessageResource.class)
				.path(String.valueOf(messageId))
				.build()
				.toString();
	}
	
	private String getUriForComments(Message message, UriInfo uriInfo) {
		return  uriInfo.getBaseUriBuilder()
						.path(MessageResource.class)
						.path(MessageResource.class, "getCommentResource")
						.path(CommentResource.class)
						.resolveTemplate("messageId", message.getId())
						.build()
						.toString();
	}
	
	private String getUriForProfile(Message message, UriInfo uriInfo){
		return  uriInfo.getBaseUriBuilder()
				.path(ProfileResource.class)
				.path(message.getAuthor())
				.build()
				.toString();
	}
}