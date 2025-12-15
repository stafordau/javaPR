GradeBook FX — клиент на JavaFX, работающий через REST API.

Запуск:
1) Подними Spring Boot приложение (REST endpoints: /api/grades).
2) В этом проекте проверь src/main/resources/app.properties:
   api.baseUrl=http://localhost:8080
3) Запусти:
   mvn javafx:run

Функции:
- список оценок
- добавить
- редактировать (двойной клик по строке или кнопка "Редактировать")
- удалить
