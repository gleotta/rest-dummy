/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cuyum.adubo.rest;

import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a Dummy RESTful service to read/write.
 */
@Path("/service")
@RequestScoped
public class DummyRESTService {

    @Inject
    private Logger log;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@QueryParam("idSolicitud") String idSolicitud, @Context UriInfo request) {
    	log.info("Se recibe por GET los siguientes parametros: ");
    	
    	Set<String> keys = request.getQueryParameters().keySet();
    	for (String key : keys) {
    		log.info(key+":"+request.getQueryParameters().getFirst(key));
        	
		}
    	if(idSolicitud==null || idSolicitud.isEmpty()){
    		return "nook";
    	}
    	
        return  "ok";
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String post(String body) {

        log.info("Se recibe por POST el siguiente body: "+body);

        return  "ok";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/date")
    public String date(@Context UriInfo request) {
	log.info("Se recibe por GET los siguientes parametros: ");
        Set<String> keys = request.getQueryParameters().keySet();
        for (String key : keys) {
            log.info(key+":"+request.getQueryParameters().getFirst(key));
        }
        return "10";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/date/random")
    public String dateRandom(@Context UriInfo request) {
        log.info("Se recibe por GET los siguientes parametros: ");
        Set<String> keys = request.getQueryParameters().keySet();
        for (String key : keys) {
            log.info(key+":"+request.getQueryParameters().getFirst(key));
        }
        Random r = new Random();
        int a = r.nextInt(10) + 5;
        return "" + a;
    }
}
