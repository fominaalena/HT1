package app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phone {

	// Данные записи о телефоне
	private String id = "";
	private String owner = "";
	private String number = "";

	// Конструктор с полями
	public Phone(String id, String owner, String number) {
		super();
		this.id = id;
		this.owner = owner;
		this.number = number;
	}

	// Конструктор для пустой записи
	public Phone() {
		super();
		this.id = "0";
		this.owner = "";
		this.number = "";
	}

	// Конструктор записи для добавления в БД
	public Phone(String owner, String number) {
		super();
		this.id = "0";
		this.owner = owner;
		this.number = number;
	}

	// Валидация номера телефона
	public boolean validatePhoneNumber(String number, boolean empty_allowed) {
		if (empty_allowed) {
			Matcher matcher = Pattern.compile("[0-9#-+]{2,50}").matcher(number);
			return matcher.matches();
		} else {
			Matcher matcher = Pattern.compile("[0-9#-+]{2,50}").matcher(number);
			return matcher.matches();
		}
	}

	// Геттеры и сеттеры
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
