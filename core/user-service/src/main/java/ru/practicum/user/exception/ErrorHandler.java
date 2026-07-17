package ru.practicum.user.exception;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	@ResponseBody
	public ErrorResponse handleUserNameAlreadyExistException(final NameAlreadyExistException exception) {
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
	public ErrorResponse handleEmptyResultDataAccessException(final EmptyResultDataAccessException exception) {
		return new ErrorResponse(exception.getMessage());
	}

	@ExceptionHandler
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
		return new ErrorResponse(exception.getMessage());
	}
}
