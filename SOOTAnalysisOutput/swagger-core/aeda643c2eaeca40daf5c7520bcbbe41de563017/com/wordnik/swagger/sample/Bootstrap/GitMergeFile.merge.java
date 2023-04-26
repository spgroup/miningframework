package com.wordnik.swagger.sample;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.auth.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {

<<<<<<< MINE
    @Override
    public void init(ServletConfig config) throws ServletException {
        Info info = new Info().title("Swagger Sample App").description("This is a sample server Petstore server.  You can find out more about Swagger " + "at <a href=\"http://swagger.io\">http://swagger.io</a> or on irc.freenode.net, #swagger.  For this sample, " + "you can use the api key \"special-key\" to test the authorization filters").termsOfService("http://helloreverb.com/terms/").contact(new Contact().email("apiteam@swagger.io")).license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        ServletContext context = config.getServletContext();
        Swagger swagger = new Swagger().info(info);
        swagger.securityDefinition("api_key", new ApiKeyAuthDefinition("api_key", In.HEADER));
        swagger.securityDefinition("petstore_auth", new OAuth2Definition().implicit("http://petstore.swagger.io/api/oauth/dialog").scope("read:pets", "read your pets").scope("write:pets", "modify pets in your account"));
        context.setAttribute("swagger", swagger);
=======
    static {
        FilterFactory.setFilter(new CustomFilter());
        ApiInfo info = new ApiInfo("Swagger Sample App", "This is a sample server Petstore server.  You can find out more about Swagger " + "at <a href=\"http://swagger.io\">http://swagger.io</a> or on irc.freenode.net, #swagger.  For this sample, " + "you can use the api key \"special-key\" to test the authorization filters", "http://helloreverb.com/terms/", "apiteam@wordnik.com", "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0.html");
        List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
        scopes.add(new AuthorizationScope("email", "Access to your email address"));
        scopes.add(new AuthorizationScope("pets", "Access to your pets"));
        List<GrantType> grantTypes = new ArrayList<GrantType>();
        ImplicitGrant implicitGrant = new ImplicitGrant(new LoginEndpoint("http://localhost:8002/oauth/dialog"), "access_code");
        grantTypes.add(implicitGrant);
        AuthorizationType oauth = new OAuthBuilder().scopes(scopes).grantTypes(grantTypes).build();
        ConfigFactory.config().addAuthorization(oauth);
        ConfigFactory.config().setApiInfo(info);
>>>>>>> YOURS
    }
}
