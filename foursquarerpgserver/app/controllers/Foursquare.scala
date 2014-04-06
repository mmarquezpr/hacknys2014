package controllers

import fi.foyt.foursquare.api.FoursquareApi
import other.FoursquareCredentials
import play.api.mvc.Action
import play.api.mvc.Controller
import com.google.gson.Gson
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.http.json.JsonHttpContent
import scala.collection.mutable.HashMap
import com.google.api.client.http.HttpHeaders
import fi.foyt.foursquare.api.entities.Checkin
import play.api.libs.json.Json

object Foursquare extends Controller {
	val fsq = new FoursquareApi(FoursquareCredentials.CLIENT_ID, FoursquareCredentials.CLIENT_SECRET, FoursquareCredentials.PUSH_SECRET);
		  val requestFactory = new NetHttpTransport().createRequestFactory();
		  val gson = new Gson();
	def redirectGet(code: String) = Action {
		println(code)
		fsq.authenticateCode(code)
		Ok("{\"token\":" +fsq.getOAuthToken() +  "}").as("application/json");
	}

	def some = Action {
		Redirect(fsq.getAuthenticationUrl());
	}
	def redirectPost = Action {
		//	      request => values = request.body.asFormUrlEncoded.get;

		Redirect(fsq.getAuthenticationUrl());
	}
	def sendToken(token: String) = Action {

	  fsq.setoAuthToken(token);
	  val username = fsq.user("self").getResult().getId();
	  Ok(gson.toJson(Database.addUser(username, token)))
	}
	def handlePush = Action {
		request => val values = request.body.asFormUrlEncoded.get;
		println(values);
				val checkinId = values.get("checkin").get(0)
						val js = Json.parse(checkinId);
				val fsqVenueId = js.\("venue").\("id").as[String];
				val categoryList:List[String] = (js.\\("parents")(0).as[List[String]]);
				val category = categoryList(0);
				val name = (js \ ("venue")).\("name").as[String];
				println(fsqVenueId);
				println(category);
				println(name);
				var dbId = Database.getVenueIdFromFsqVenueId(fsqVenueId);
				if(!dbId.isDefined) {
					Database.addVenue(fsqVenueId, category, name);
					dbId = Database.getVenueIdFromFsqVenueId(fsqVenueId);
				}
				sendPush();
				Ok(views.html.index("Success!"))
	}
	
	def getUserGet(username: String) = Action {
	  Ok(gson.toJson(Database.getUser(username)));
	}
	def sendPush() {
		val map = new HashMap[String,Array[String]]();
		map += "registrations_ids" -> Array("APA91bFTwIqwYXxPCg9IOKN28K-M6l5FhcBFw8SBzPr1925ndG07SAVIPGv9MyiNCZpt4WDNvIsowPjOnGKwlm4bUGu07xPZZ7JteU8amPxN9NZUfxCJ-dPDjbYT8FZdJ99xqg6y8HU9ZOrkUb8KEh-bmPtcX2iCkVJ5VI2xrUtDocfWLspLfHE");
		println(map);
		val json = new JsonHttpContent(new JacksonFactory(), "{\"registration_ids\":[\"APA91bFTwIqwYXxPCg9IOKN28K-M6l5FhcBFw8SBzPr1925ndG07SAVIPGv9MyiNCZpt4WDNvIsowPjOnGKwlm4bUGu07xPZZ7JteU8amPxN9NZUfxCJ-dPDjbYT8FZdJ99xqg6y8HU9ZOrkUb8KEh-bmPtcX2iCkVJ5VI2xrUtDocfWLspLfHE\"]}");
		val post = requestFactory.buildPostRequest(new GenericUrl("https://android.googleapis.com/gcm/send"), json);
		val header = new HttpHeaders();
		header.setContentType("application/json");
		header.setAuthorization("key=AIzaSyAe1TgUXvuoWJTcYPNCIvCgM0r4yD4MnC0");
		post.setHeaders(header);
		post.execute();
	  
	}
}