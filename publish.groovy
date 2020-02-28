@Grapes([
    @Grab(group='com.google.apis', module='google-api-services-calendar', version='v3-rev361-1.25.0'),
    @Grab(group='com.google.http-client', module='google-http-client-jackson2', version='1.27.0'),
    @Grab(group='com.google.oauth-client', module='google-oauth-client-jetty', version='1.27.0')
])

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.client.util.DateTime

def APPLICATION_NAME      = "google-calendar-publisher"

def props = new Properties()
def propsfile = new File("/gcal.properties")

propsfile.withInputStream {
  props.load(it)
}

def OAUTH_CLIENT_ID       = props.OAUTH_CLIENT_ID
def OAUTH_CLIENT_SECRET   = props.OAUTH_CLIENT_SECRET
def OAUTH_ACCESS_TOKEN    = props.OAUTH_ACCESS_TOKEN
def OAUTH_REFRESH_TOKEN   = props.OAUTH_REFRESH_TOKEN
def GCAL_ID               = props.GCAL_ID

def GCAL_EVENT_SUMMARY    = System.getenv("GCAL_EVENT_SUMMARY")
def GCAL_EVENT_DESCRIPTION = System.getenv("GCAL_EVENT_DESCRIPTION")
// Timestamps in ms since epoch, e.g. new Date().getTime()
def GCAL_EVENT_START_TIME = System.getenv("GCAL_EVENT_START_TIME")
def GCAL_EVENT_END_TIME   = System.getenv("GCAL_EVENT_END_TIME")

if (OAUTH_CLIENT_ID == null) {
  println "No OAUTH_CLIENT_ID found in properties file. Check your gcal.properties"
  System.exit(0)
}

def gcs = new GoogleClientSecrets().setInstalled(new GoogleClientSecrets.Details()
  .setClientId(OAUTH_CLIENT_ID)
  .setClientSecret(OAUTH_CLIENT_SECRET)
  .setAuthUri("https://accounts.google.com/o/oauth2/auth")
  .setTokenUri("https://accounts.google.com/o/oauth2/token")
  .setRedirectUris([ "urn:ietf:wg:oauth:2.0:oob" ])
)

def dsFactory = MemoryDataStoreFactory.getDefaultInstance()

dsFactory.getDataStore("StoredCredential").set("user", new StoredCredential()
         .setAccessToken(OAUTH_ACCESS_TOKEN)
         .setRefreshToken(OAUTH_REFRESH_TOKEN)
         .setExpirationTimeMilliseconds(1491480728089L))

def JSON_FACTORY = JacksonFactory.getDefaultInstance()
def httpTransport = GoogleNetHttpTransport.newTrustedTransport()
def flow = new GoogleAuthorizationCodeFlow.Builder(
                  httpTransport,
                  JSON_FACTORY,
                  gcs,
                  Collections.singleton(CalendarScopes.CALENDAR))
        .setDataStoreFactory(dsFactory)
        .build()

def creds = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")

def client = new com.google.api.services.calendar.Calendar.Builder(
          httpTransport, JSON_FACTORY, creds).setApplicationName(APPLICATION_NAME).build()

def foundCal = client.calendars().get(GCAL_ID).execute()

def evt = new Event()
def startTime = new Date(Long.parseLong(GCAL_EVENT_START_TIME))
def stopTime = new Date(Long.parseLong(GCAL_EVENT_END_TIME))

evt.setSummary(GCAL_EVENT_SUMMARY)
evt.setDescription(GCAL_EVENT_DESCRIPTION)
evt.setStart(new EventDateTime().setDateTime(new DateTime(startTime, TimeZone.getTimeZone("Europe/Paris"))))
evt.setEnd(new EventDateTime().setDateTime(new DateTime(stopTime, TimeZone.getTimeZone("Europe/Paris"))))

client.events().insert(foundCal.getId(), evt).execute()
println("Done.")
