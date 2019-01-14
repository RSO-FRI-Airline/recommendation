package si.fri.rso.api;

import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.configuration.sources.FileConfigurationSource;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Path("recommend")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class RecommendationEndpoint {

    @Inject
    @DiscoverService(value = "rso-airline-search")
    private Optional<String> baseUrl;

    @GET
    @ApplicationScoped
    @Path("/")
    public Response get(){
        try{
            Random r = new Random();
            JSONArray destinations = new JSONArray(GetDestinations());
            List<Object> dests = destinations.toList();
            dests.remove(0);
            dests.sort((m1, m2) -> r.nextInt());
            ArrayList<Object> newList = new ArrayList<>();
            for(int i = 0; i < 3; i++) {
                HashMap o = (HashMap) dests.get(i);

                Date date = new java.util.Date();
                date.setTime(date.getTime()+((1+r.nextInt(20))*1000*60*60*24));
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
                String formattedDate = sdf.format(date);

                o.put("date", formattedDate);
                newList.add(o);
            }

            return  Response.ok(newList).build();
        }
        catch (Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    public String GetDestinations() throws Exception{
        URL url = new URL(baseUrl.get()+"/v1/airports");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }
}
