package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagePersonServlet extends HttpServlet {

	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;

	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;

	public ManagePersonServlet() {
		// Вызов родительского конструктора.
		super();

		// Создание экземпляра телефонной книги.
		try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
	private String validatePersonFMLName(Person person) {
		String error_message = "";

		if (!person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		if (!person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}

		return error_message;
	}

	// Валидация телефона и генерация сообщения об ошибке в случае невалидных данных
	private String validatePhoneNumber(Phone phone) {
		String error_message = "";

		if (!phone.validatePhoneNumber(phone.getNumber(), false)) {
			error_message += "Номер должен быть длиной от 2 до 50 символов и содержать цифры 0-9 и символы +, -, #.<br />";
		}

		return error_message;
	}

	// Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в
		// "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в
		// JSP.
		request.setAttribute("phonebook", this.phonebook);

		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления
		// (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_phone = request.getRequestDispatcher("/ManagePhones.jsp");

		// Действие (action) и идентификатор записи (id) над которой выполняется это
		// действие.
		String action = request.getParameter("action");
		String id = request.getParameter("id");

		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
		if ((action == null) && (id == null)) {
			request.setAttribute("jsp_parameters", jsp_parameters);
			dispatcher_for_list.forward(request, response);
		}
		// Если же действие указано, то...
		else {
			switch (action) {
			// Добавление записи.
			case "add":
				// Создание новой пустой записи о пользователе.
				Person empty_person = new Person();

				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "add");
				jsp_parameters.put("next_action", "add_go");
				jsp_parameters.put("next_action_label", "Добавить");

				// Установка параметров JSP.
				request.setAttribute("person", empty_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
				break;

			// Редактирование записи.
			case "edit":
				// Извлечение из телефонной книги информации о редактируемой записи.
				Person editable_person = this.phonebook.getPerson(id);

				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");

				// Установка параметров JSP.
				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
				break;

			// Удаление записи.
			case "delete":

				// Если запись удалось удалить...
				if (phonebook.deletePerson(id)) {
					jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
				}
				// Если запись не удалось удалить (например, такой записи нет)...
				else {
					jsp_parameters.put("current_action_result", "DELETION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
				break;

			// Вернуться к списку людей из добавления информации о человеке
			case "back_to_list":
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_list.forward(request, response);
				break;

			// Вернуться к списку из редактирования информации о человеке
			case "back_to_managePerson":
				// Извлечение из телефонной книги информации о редактируемой записи.
				Person editable_person1 = this.phonebook.getPerson(id);

				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");

				// Установка параметров JSP.
				request.setAttribute("person", editable_person1);
				request.setAttribute("jsp_parameters", jsp_parameters);


			case "add_phone":
				// Создание новой пустой записи о телефоне и инициализация человека, телефон
				// которого будет добавлен
				Phone empty_phone = new Phone();
				Person editable_person2 = this.phonebook.getPerson(id);
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "add_phone");
				jsp_parameters.put("id_person", editable_person2.getId());
				jsp_parameters.put("next_action", "add_phone_go");
				jsp_parameters.put("next_action_label", "Добавить номер");

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", editable_person2);

				// Передача запроса в JSP.
				dispatcher_for_phone.forward(request, response);

				break;

			case "edit_phone":
				// Извлечение из телефонной книги информации о редактируемой записи.

				Phone editable_phone = this.phonebook.getPhone(id);
				
				if (phonebook.updatePhone(id, editable_phone)) {
					jsp_parameters.put("current_action", "edit");
					jsp_parameters.put("id_phone", editable_phone.getNumber());
					jsp_parameters.put("next_action", "edit_phone_go");
					jsp_parameters.put("next_action_label", "Сохранить");
				}

				// Установка параметров JSP.
				request.setAttribute("person", editable_phone);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_phone.forward(request, response);

				break;

			case "delete_phone":
				// Если запись удалось удалить...
				if (phonebook.deletePhone(id)) {
					jsp_parameters.put("current_action", "DELETION_SUCCESS");
					jsp_parameters.put("current_action_label", "Удаление выполнено успешно");
				}
				// Если запись не удалось удалить (например, такой записи нет)...
				else {
					jsp_parameters.put("current_action", "DELETION_FAILURE");
					jsp_parameters.put("current_action_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
				break;

			}
		}

	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в
		// "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в
		// JSP.
		request.setAttribute("phonebook", this.phonebook);

		// Хранилище параметров для передачи в JSP.
		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления
		// (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_phone = request.getRequestDispatcher("/ManagePhone.jsp");

		// Действие (add_go, edit_go) и идентификатор записи (id) над которой
		// выполняется это действие.
		String add_go = request.getParameter("add_go");
		String edit_go = request.getParameter("edit_go");
		String id = request.getParameter("id");
		String add_phone_go = request.getParameter("add_phone_go");
		String edit_phone_go = request.getParameter("edit_phone_go");

		// Добавление записи.
		if (add_go != null) {
			// Создание записи на основе данных из формы.
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"),
					request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(new_person);

			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {

				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person)) {
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else {
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {
				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "add");
				jsp_parameters.put("next_action", "add_go");
				jsp_parameters.put("next_action_label", "Добавить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
				request.setAttribute("person", new_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}

		// Редактирование записи.
		if (edit_go != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id"));
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(updatable_person);

			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {

				// Если запись удалось обновить...
				if (this.phonebook.updatePerson(id, updatable_person)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else {

				// Подготовка параметров для JSP.
				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);

				// Установка параметров JSP.
				request.setAttribute("person", updatable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);

			}
		}
		// Добавление записи о телефоне
		if (add_phone_go != null) {
			{
				String id_person = request.getParameter("id");
				Person editable_person = this.phonebook.getPerson(id_person);
				Phone phone = new Phone(id_person, request.getParameter("number"));

				// Добавление записи
				this.phonebook.addPhone(phone);
				editable_person = this.phonebook.getPerson(id_person);
				// Подготовка параметров для JSP.

				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");

				// Установка параметров JSP.
				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_manager.forward(request, response);
			}
		}

		// Редактирование записи о телефоне.
		if (edit_phone_go != null) {

			// Получение записи и её обновление на основе данных из формы.
			Phone updatable_phone = this.phonebook.getPhone(request.getParameter("id"));
			updatable_phone.setNumber(request.getParameter("number"));

			// Валидация телефона.
			String error_message = this.validatePhoneNumber(updatable_phone);

			// Если данные верные, можно производить добавление.
			if (error_message.equals("")) {

				// Если запись удалось обновить...
				if (this.phonebook.updatePhone(id, updatable_phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}
				jsp_parameters.put("current_action", "edit");
				jsp_parameters.put("next_action", "edit_go");
				jsp_parameters.put("next_action_label", "Сохранить");
				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);

				// Передача запроса в JSP.
				dispatcher_for_phone.forward(request, response);
			}
		}
	}
}
