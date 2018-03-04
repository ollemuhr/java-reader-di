package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.Test;

/**
 *
 */
public class ValidationTest
{
	@Test
	public void testValidation()
	{
		Validation<Seq<String>, Person> validatedPerson =
			Person.valid("Mario", 40);
		System.out.println(validatedPerson);
	}

	@Test
	public void testValidationFail()
	{
		Validation<Seq<String>, Person> validatedPerson =
			Person.valid("olle", 150);
		System.out.println(validatedPerson);
	}

	public static Validation<String, Integer> validAge(int a)
	{
		return isValidAge(a) ? Validation.valid(a) : Validation.invalid("Age must be less than 130");
	}

	public static boolean isValidAge(int age)
	{
		return age < 130;
	}

	public static Validation<String, String> validName(String s)
	{
		return isValidName(s) ? Validation.valid(s) : Validation.invalid("Name must start with an uppercase");
	}

	public static boolean isValidName(String s)
	{
		return Character.isUpperCase(s.charAt(0));
	}

	public static class Person
	{
		private final String name;
		private final int age;

		private Person(String name, int age)
		{
			this.name = name;
			this.age = age;
		}

		public static Validation<Seq<String>, Person> valid(final String name, final int age)
		{
			return validName(name)
				.combine(validAge(age))
				.ap(Person::new);
		}

		public int getAge()
		{
			return age;
		}

		public String getName()
		{
			return name;
		}

		@Override
		public String toString()
		{
			return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
		}
	}
}
