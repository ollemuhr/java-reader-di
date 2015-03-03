package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;
import org.junit.Test;

import java.util.List;

import static com.github.ollemuhr.validation.Validation.failure;
import static com.github.ollemuhr.validation.Validation.success;

/**
 *
 */
public class ValidationTest {
    @Test
    public void testValidation() {
        Person person = new Person("Mario", 40);
        Validation<List<Object>, Person> validatedPerson =
                success(person).failList()
                        .flatMap(ValidationTest::validAge)
                        .flatMap(ValidationTest::validName);

        System.out.println(validatedPerson);
        System.out.println(validatedPerson.value());
    }

    @Test
    public void testValidationFail() {
        Person person = new Person("olle", 150);
        Validation<List<Object>, Person> validatedPerson =
                success(person).failList()
                        .flatMap(ValidationTest::validAge)
                        .flatMap(ValidationTest::validName);

        System.out.println(validatedPerson);
        System.out.println(validatedPerson.value());
        System.out.println(validatedPerson.failure());
    }

    public static Validation<String, Person> validAge(Person p) {
        return isValidAge(p) ? success(p) : failure("Age must be less than 130", p);
    }

    public static boolean isValidAge(Person p) {
        return p.getAge() < 130;
    }

    public static Validation<String, Person> validName(Person p) {
        return isValidName(p) ? success(p) :  failure("Name must start with an uppercase", p);
    }

    public static boolean isValidName(Person p) {
        return Character.isUpperCase(p.getName().charAt(0));
    }

    public static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
