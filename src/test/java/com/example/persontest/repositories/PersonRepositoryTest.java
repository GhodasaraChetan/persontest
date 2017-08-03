package com.example.persontest.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.persontest.domain.Person;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonRepositoryTest {

	/**
	 * Error:
	 * no suitable HttpMessageConverter found for response type Resources<Resource<Person>>
	 */
	@Test
	public void clientCall_test() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.registerModule(new Jackson2HalModule());

		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<>();
		supportedMediaTypes.add(MediaTypes.HAL_JSON);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON);
		messageConverter.setSupportedMediaTypes(supportedMediaTypes);
		messageConverter.setObjectMapper(objectMapper);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(Arrays.asList(messageConverter));
		ResponseEntity<Resources<Resource<Person>>> getResult = restTemplate.exchange("http://localhost:8080/persons",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Resources<Resource<Person>>>() {});
		Resources<Resource<Person>> body = getResult.getBody();

		ResponseEntity<Resource<Person>> getSingleResult = restTemplate.exchange("http://localhost:8080/persons/1",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Resource<Person>>() {});
		Resource<Person> singleElementBody = getSingleResult.getBody();

		assertEquals(1, 1);
	}

	/**
	 * Jackson-databind 2.9.1-SNAPSHOT:
	 * When we register Jackson2HalModule and try to deserialize value for "Resources" class,
	 * it throws NullPointerException.
	 *
	 * @throws IOException
	 */
	@Test
	public void objectMapper_canDeserialize_test() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Jackson2HalModule());
		JavaType javaType = objectMapper.getTypeFactory().constructType(Resources.class);
		assertTrue(objectMapper.canDeserialize(javaType, null));
	}
}