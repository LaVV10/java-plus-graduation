package ru.practicum.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleRequestAlreadyExistException(final RequestAlreadyExistException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleRequestAlreadyConfirmedException(final RequestAlreadyConfirmedException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleParticipantLimitException(final ParticipantLimitException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleEventIsNotPublishedException(final EventIsNotPublishedException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleWrongUserException(final WrongUserException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleRequestNotExistException(final RequestNotExistException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleUserNotExistException(final UserNotExistException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ResponseBody
	public ErrorResponse handleEventNotExistException(final EventNotExistException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleWrongDataException(final WrongDataException exception) {
		return new ErrorResponse(exception.getMessage());
	}
}
