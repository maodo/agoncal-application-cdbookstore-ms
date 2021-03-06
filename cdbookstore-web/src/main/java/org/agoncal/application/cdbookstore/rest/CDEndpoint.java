package org.agoncal.application.cdbookstore.rest;

import org.agoncal.application.cdbookstore.model.CD;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Path("/cds")
@Transactional
public class CDEndpoint {

    // ======================================
    // =          Injection Points          =
    // ======================================

    @Inject
    private EntityManager em;

    // ======================================
    // =          Business methods          =
    // ======================================

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response create(CD entity) {
        em.persist(entity);
        return Response.created(
            UriBuilder.fromResource(CDEndpoint.class)
                .path(String.valueOf(entity.getId())).build())
            .build();
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") Long id) {
        CD entity = em.find(CD.class, id);
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        em.remove(entity);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces({"application/xml", "application/json"})
    public Response findById(@PathParam("id") Long id) {
        TypedQuery<CD> findByIdQuery = em
            .createQuery(
                "SELECT DISTINCT c FROM CD c LEFT JOIN FETCH c.label LEFT JOIN FETCH c.musicians LEFT JOIN FETCH c.genre WHERE c.id = :entityId ORDER BY c.id",
                CD.class);
        findByIdQuery.setParameter("entityId", id);
        CD entity;
        try {
            entity = findByIdQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(entity).build();
    }

    @GET
    @Produces({"application/xml", "application/json"})
    public List<CD> listAll(@QueryParam("start") Integer startPosition,
                            @QueryParam("max") Integer maxResult) {
        TypedQuery<CD> findAllQuery = em
            .createQuery(
                "SELECT DISTINCT c FROM CD c LEFT JOIN FETCH c.label LEFT JOIN FETCH c.musicians LEFT JOIN FETCH c.genre ORDER BY c.id",
                CD.class);
        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }
        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }
        final List<CD> results = findAllQuery.getResultList();
        return results;
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes({"application/xml", "application/json"})
    public Response update(@PathParam("id") Long id, CD entity) {
        if (entity == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!id.equals(entity.getId())) {
            return Response.status(Status.CONFLICT).entity(entity).build();
        }
        if (em.find(CD.class, id) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        try {
            entity = em.merge(entity);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }
}
