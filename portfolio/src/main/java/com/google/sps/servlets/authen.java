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


@WebServlet("/authen")
public class authen extends HttpServlet {
  GsonBuilder gsonBuilder = new GsonBuilder();
  Gson gson = gsonBuilder.create();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    UserService userService = UserServiceFactory.getUserService();
    ArrayList<String> loggedIn = new ArrayList<String>();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/blogs/gear.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      loggedIn.add("yes");
      loggedIn.add(logoutUrl);
      if(userService.isUserAdmin()) {
          loggedIn.add("admin");
      }
      loggedIn.add("not admin");
      
      response.getWriter().println(gson.toJson(loggedIn));
    } else {
      String urlToRedirectToAfterUserLogsIn = "/blogs/gear.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      loggedIn.add("no");
      loggedIn.add(loginUrl);
      loggedIn.add("not admin");

      response.getWriter().println(gson.toJson(loggedIn));
    }
  }
}
