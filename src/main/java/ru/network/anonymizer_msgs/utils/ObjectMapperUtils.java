package ru.network.anonymizer_msgs.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class ObjectMapperUtils {
	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T convert(String fullFileName, Class<T> neededClass) {
		ClassPathResource classPathResource = new ClassPathResource(fullFileName);
		try (InputStream inputStream = classPathResource.getInputStream()) {
			return objectMapper.readValue(inputStream, neededClass);
		} catch (IOException e) {
			log.error("Failed to load file: {}", classPathResource, e);
			return null;
		}
	}

	public static <T> T convertNotNull(String fullFileName, Class<T> neededClass) {
		return Objects.requireNonNull(convert(fullFileName, neededClass));
	}

	@SneakyThrows
	public static String writeString(Object any) {
		return objectMapper.writeValueAsString(any);
	}

	@SneakyThrows
	public static <T> T readValue(String json, Class<T> typeClass) {
		return objectMapper.readValue(json, typeClass);
	}

}

