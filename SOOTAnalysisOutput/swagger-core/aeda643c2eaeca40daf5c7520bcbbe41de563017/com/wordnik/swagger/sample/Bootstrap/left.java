package com.wordnik.swagger.sample;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.auth.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        Info info = new Info().title("Swagger Sample App").description("This is a sample server Petstore server.  You can find out more about Swagger " + "at <a href=\"http://swagger.io\">http://swagger.io</a> or on irc.freenode.net, #swagger.  For this sample, " + "you can use the api key \"special-key\" to test the authorization filters").termsOfService("http://helloreverb.com/terms/").contact(new Contact().email("apiteam@swagger.io")).license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        ServletContext context = config.getServletContext();
        Swagger swagger = new Swagger().info(info);
        swagger.securityDefinition("api_key", new ApiKeyAuthDefinition("api_key", In.HEADER));
        swagger.securityDefinition("petstore_auth", new OAuth2Definition().implicit("http://petstore.swagger.io/api/oauth/dialog").scope("read:pets", "read your pets").scope("write:pets", "modify pets in your account"));
        context.setAttribute("swagger", swagger);
    }
}
