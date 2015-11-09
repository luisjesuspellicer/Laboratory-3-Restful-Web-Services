package rest.addressbook;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.After;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import rest.addressbook.AddressBook;
import rest.addressbook.ApplicationConfig;
import rest.addressbook.Person;

/**
 * A simple test suite
 *
 */
public class AddressBookServiceTest {

	HttpServer server;

	@Test
	public void serviceIsAlive() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		launchServer(ab);

		// Request the address book
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request().get();

		assertEquals(200, response.getStatus());
		assertEquals(0, response.readEntity(AddressBook.class).getPersonList().size());

		//////////////////////////////////////////////////////////////////////
		// Verify that GET /contacts is well implemented by the service, i.e
		// test that it is safe and idempotent
		//////////////////////////////////////////////////////////////////////	
		
		Response otherResponse = client.target("http://localhost:8282/contacts").request().get();
		// Check that GET is idempotent and safe. Same results.
		assertEquals(200,response.getStatus());

		assertEquals(0,otherResponse.readEntity(AddressBook.class).getPersonList().size());

	}

	@Test
	public void createUser() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		launchServer(ab);

		// Prepare data
		Person juan = new Person();
		juan.setName("Juan");
		URI juanURI = URI.create("http://localhost:8282/contacts/person/1");

		// Create a new user
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan, MediaType.APPLICATION_JSON));

		assertEquals(201, response.getStatus());
		assertEquals(juanURI, response.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person juanUpdated = response.readEntity(Person.class);
		assertEquals(juan.getName(), juanUpdated.getName());
		assertEquals(1, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		// Check that the new user exists
		response = client.target("http://localhost:8282/contacts/person/1")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		juanUpdated = response.readEntity(Person.class);
		assertEquals(juan.getName(), juanUpdated.getName());
		assertEquals(1, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		//////////////////////////////////////////////////////////////////////
		// Verify that POST /contacts is well implemented by the service, i.e
		// test that it is not safe and not idempotent
		//////////////////////////////////////////////////////////////////////	


		// Prepare data
		Person luis = new Person();
		luis.setName("Luis");
		URI luisURI = URI.create("http://localhost:8282/contacts/person/2");

		// Create a new user
		Client clientt = ClientBuilder.newClient();
		Response responsee = clientt.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(luis, MediaType.APPLICATION_JSON));

		assertEquals(201, responsee.getStatus());
		assertEquals(luisURI, responsee.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, responsee.getMediaType());
		Person luisUpdated = responsee.readEntity(Person.class);
		assertEquals(luis.getName(), luisUpdated.getName());
		assertEquals(2, luisUpdated.getId());
		assertEquals(luisURI, luisUpdated.getHref());

		// Check that the new user exists
		responsee = clientt.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, responsee.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, responsee.getMediaType());
		luisUpdated = responsee.readEntity(Person.class);
		assertEquals(luis.getName(), luisUpdated.getName());
		assertEquals(2, luisUpdated.getId());
		assertEquals(luisURI, luisUpdated.getHref());

		// POST can make changes -> not safe.

		// Second check that POST it's not idempotent. Different results.


		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		clientt = ClientBuilder.newClient();
		responsee = clientt.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(luis, MediaType.APPLICATION_JSON));
		
		assertEquals(201,responsee.getStatus());
		Person luisUpdated2 = responsee.readEntity(Person.class);

		// Different results
		assertNotEquals(luisUpdated.getId(),luisUpdated2.getId());

	}

	@Test
	public void createUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(ab.nextId());
		ab.getPersonList().add(salvador);
		launchServer(ab);

		// Prepare data
		Person juan = new Person();
		juan.setName("Juan");
		URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
		Person maria = new Person();
		maria.setName("Maria");
		URI mariaURI = URI.create("http://localhost:8282/contacts/person/3");

		// Create a user
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan, MediaType.APPLICATION_JSON));
		assertEquals(201, response.getStatus());
		assertEquals(juanURI, response.getLocation());

		// Create a second user
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(201, response.getStatus());
		assertEquals(mariaURI, response.getLocation());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person mariaUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaUpdated.getName());
		assertEquals(3, mariaUpdated.getId());
		assertEquals(mariaURI, mariaUpdated.getHref());

		// Check that the new user exists
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		mariaUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaUpdated.getName());
		assertEquals(3, mariaUpdated.getId());
		assertEquals(mariaURI, mariaUpdated.getHref());

		//////////////////////////////////////////////////////////////////////
		// Verify that GET /contacts/person/3 is well implemented by the service, i.e
		// test that it is safe and idempotent
		//////////////////////////////////////////////////////////////////////	
		Client clientt = ClientBuilder.newClient();
		Response responsee = clientt.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		Person mariaUpdated2 = responsee.readEntity(Person.class);
		// Same results.
		assertEquals(mariaUpdated.getId(),mariaUpdated2.getId());
		assertEquals(mariaUpdated.getHref(),mariaUpdated2.getHref());
	}

	@Test
	public void listUsers() throws IOException {

		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		Person juan = new Person();
		juan.setName("Juan");
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Test list of contacts
		Client client = ClientBuilder.newClient();
		Response response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		AddressBook addressBookRetrieved = response
				.readEntity(AddressBook.class);
		assertEquals(2, addressBookRetrieved.getPersonList().size());
		assertEquals(juan.getName(), addressBookRetrieved.getPersonList()
				.get(1).getName());

		//////////////////////////////////////////////////////////////////////
		// Verify that POST is well implemented by the service, i.e
		// test that it is not safe and not idempotent
		//////////////////////////////////////////////////////////////////////	
		Person juan2 = new Person();
		juan2.setName("juan");
		Client clientt = ClientBuilder.newClient();
		response = clientt.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan2, MediaType.APPLICATION_JSON));
		response = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		addressBookRetrieved = response
				.readEntity(AddressBook.class);
		
		assertEquals(3,addressBookRetrieved.getPersonList().size());
		
		Client clientt2 = ClientBuilder.newClient();
		Response responsee2 = clientt2.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(juan2, MediaType.APPLICATION_JSON));
		responsee2 = client.target("http://localhost:8282/contacts")
				.request(MediaType.APPLICATION_JSON).get();
		AddressBook addressBookRetrieved2 = responsee2
				.readEntity(AddressBook.class);
		//Results not equal and get is idempotent -> POST not safe and not idempotent.
		assertNotEquals(addressBookRetrieved.getPersonList().size(),addressBookRetrieved2.getPersonList().size());
	}

	@Test
	public void updateUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(ab.nextId());
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(ab.getNextId());
		URI juanURI = URI.create("http://localhost:8282/contacts/person/2");
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Update Maria
		Person maria = new Person();
		maria.setName("Maria");
		Client client = ClientBuilder.newClient();
		Response response = client
				.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person juanUpdated = response.readEntity(Person.class);
		assertEquals(maria.getName(), juanUpdated.getName());
		assertEquals(2, juanUpdated.getId());
		assertEquals(juanURI, juanUpdated.getHref());

		// Verify that the update is real
		response = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person mariaRetrieved = response.readEntity(Person.class);
		assertEquals(maria.getName(), mariaRetrieved.getName());
		assertEquals(2, mariaRetrieved.getId());
		assertEquals(juanURI, mariaRetrieved.getHref());

		// Verify that only can be updated existing values
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(maria, MediaType.APPLICATION_JSON));
		assertEquals(400, response.getStatus());

		//////////////////////////////////////////////////////////////////////
		// Verify that PUT /contacts/person/2 is well implemented by the service, i.e
		// test that it is idempotent
		//////////////////////////////////////////////////////////////////////	
		// Old data
		response = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		Person old = response.readEntity(Person.class);
		Client clientt = ClientBuilder.newClient();
		Response responsee = clientt
				.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.entity(maria, MediaType.APPLICATION_JSON));
		// New data
		responsee = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		Person neww = responsee.readEntity(Person.class);
		// Same results.
		assertEquals(old.getId(),neww.getId());
		assertEquals(old.getName(),neww.getName());
		assertEquals(old.getHref(),neww.getHref());

	}

	@Test
	public void deleteUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(1);
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(2);
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Delete a user
		Client client = ClientBuilder.newClient();
		Response response = client
				.target("http://localhost:8282/contacts/person/2").request()
				.delete();
		assertEquals(204, response.getStatus());

		// Verify that the user has been deleted
		response = client.target("http://localhost:8282/contacts/person/2")
				.request().delete();
		assertEquals(404, response.getStatus());

		//////////////////////////////////////////////////////////////////////
		// Verify that DELETE /contacts/person/2 is well implemented by the service, i.e
		// test that it is idempotent
		//////////////////////////////////////////////////////////////////////	
		Client clientt = ClientBuilder.newClient();
		Response responsee = clientt.target("http://localhost:8282/contacts/person/2")
				.request().delete();
		// Same result.
		assertEquals(response.getStatus(), responsee.getStatus());
	}

	@Test
	public void findUsers() throws IOException {
		// Prepare server
		AddressBook ab = new AddressBook();
		Person salvador = new Person();
		salvador.setName("Salvador");
		salvador.setId(1);
		Person juan = new Person();
		juan.setName("Juan");
		juan.setId(2);
		ab.getPersonList().add(salvador);
		ab.getPersonList().add(juan);
		launchServer(ab);

		// Test user 1 exists
		Client client = ClientBuilder.newClient();
		Response response = client
				.target("http://localhost:8282/contacts/person/1")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		Person person = response.readEntity(Person.class);
		assertEquals(person.getName(), salvador.getName());
		assertEquals(person.getId(), salvador.getId());
		assertEquals(person.getHref(), salvador.getHref());

		// Test user 2 exists
		response = client.target("http://localhost:8282/contacts/person/2")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
		person = response.readEntity(Person.class);
		assertEquals(person.getName(), juan.getName());
		assertEquals(2, juan.getId());
		assertEquals(person.getHref(), juan.getHref());

		// Test user 3 exists
		response = client.target("http://localhost:8282/contacts/person/3")
				.request(MediaType.APPLICATION_JSON).get();
		assertEquals(404, response.getStatus());
	}

	private void launchServer(AddressBook ab) throws IOException {
		URI uri = UriBuilder.fromUri("http://localhost/").port(8282).build();
		server = GrizzlyHttpServerFactory.createHttpServer(uri,
				new ApplicationConfig(ab));
		server.start();
	}

	@After
	public void shutdown() {
		if (server != null) {
			server.shutdownNow();
		}
		server = null;
	}

}
