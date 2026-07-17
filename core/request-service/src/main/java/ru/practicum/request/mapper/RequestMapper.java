package ru.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {

	public RequestDto toRequestDto(Request request) {
		if (request == null) {
			return null;
		}

		RequestDto dto = new RequestDto();
		dto.setId(request.getId());
		dto.setCreated(request.getCreated());
		dto.setStatus(String.valueOf(request.getStatus()));
		dto.setEvent(request.getEventId());
		dto.setRequester(request.getRequesterId());
		return dto;
	}

	public List<RequestDto> toRequestDtoList(List<Request> requests) {
		if (requests == null) {
			return null;
		}

		return requests.stream()
				.map(this::toRequestDto)
				.collect(Collectors.toList());
	}
}
