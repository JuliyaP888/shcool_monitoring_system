package com.prishedko;

import com.prishedko.servlet.CourseServlet;
import com.prishedko.servlet.SchoolServlet;
import com.prishedko.servlet.StudentServlet;
import com.prishedko.servlet.TeacherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class App {

    public static void main(String[] args) throws LifecycleException {
        // Создаем экземпляр Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");

        // Настраиваем коннектор
        Connector conn = new Connector();
        conn.setPort(8080);
        tomcat.setConnector(conn);

        startUpContext(tomcat);

        // Запускаем сервер
        tomcat.start();
        System.out.println("Tomcat started on http://localhost:8080/school-service/api/schools");

        // Держим сервер запущенным
        tomcat.getServer().await();
    }

    private static void startUpContext(Tomcat tomcat) {
        // Устанавливаем контекст приложения
        String webappDir = new File("src/main/webapp").getAbsolutePath();
        Context context = tomcat.addWebapp("/school-service", webappDir);

        // Регистрируем SchoolServlet
        Tomcat.addServlet(context, "SchoolServlet", new SchoolServlet());
        context.addServletMappingDecoded("/api/schools", "SchoolServlet");
        context.addServletMappingDecoded("/api/schools/*", "SchoolServlet");

        // Регистрация TeacherServlet
        Tomcat.addServlet(context, "TeacherServlet", new TeacherServlet());
        context.addServletMappingDecoded("/api/teachers", "TeacherServlet");
        context.addServletMappingDecoded("/api/teachers/*", "TeacherServlet");

        // Регистрация StudentServlet
        Tomcat.addServlet(context, "StudentServlet", new StudentServlet());
        context.addServletMappingDecoded("/api/students", "StudentServlet");
        context.addServletMappingDecoded("/api/students/*", "StudentServlet");

        // Регистрация CourseServlet
        Tomcat.addServlet(context, "CourseServlet", new CourseServlet());
        context.addServletMappingDecoded("/api/courses", "CourseServlet");
        context.addServletMappingDecoded("/api/courses/*", "CourseServlet");
    }

}
