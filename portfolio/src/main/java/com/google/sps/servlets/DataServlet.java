// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@WebServlet("/data")
public class DataServlet extends HttpServlet {
  ArrayList<String> comments = new ArrayList<String>();
  GsonBuilder gsonBuilder = new GsonBuilder();
  Gson gson = gsonBuilder.create();
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("when", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);
    ArrayList<String> comments1 = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
        String content = (String) entity.getProperty("content");
        String name = (String) entity.getProperty("name");
        String email = (String) entity.getProperty("email");
        comments1.add("[" + name + " - " + email + "] - " + content);
        //comments1.add(email);
    }
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments1));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String first = getParameter(request, "first", "");
    String last = getParameter(request, "last", "");
    String name = first + " " + last.charAt(0) + ".";
    String comment = getParameter(request, "text-input", "");
    java.util.Date date=new java.util.Date();  
    String email = userService.getCurrentUser().getEmail();
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("when", date);
    commentEntity.setProperty("email", email);
    //add a property that is the signed in persons email, this will only be possible if the person is
    //actually signed in so it should not be a problem
    datastore.put(commentEntity);
    return;
  }
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
