package com.nau.lab7;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Component
public class FeistelNetworkRunner implements CommandLineRunner {

    private final CipherService cipherService;
    private final FileService fileService;
    private final KeyGeneratorService keyGeneratorService;

    public FeistelNetworkRunner(CipherService cipherService, FileService fileService, KeyGeneratorService keyGeneratorService) {
        this.cipherService = cipherService;
        this.fileService = fileService;
        this.keyGeneratorService = keyGeneratorService;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("Лабораторна робота №7 - МЕРЕЖА ФЕЙСТЕЛЯ");
            System.out.println("Виберіть операцію:");
            System.out.println("1. Шифрування");
            System.out.println("2. Дешифрування");
            System.out.println("3. Вийти");

            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 3) {
                System.out.println("Завершення роботи...");
                return;
            }

            System.out.println("Введіть ім'я вхідного файлу:");
            String inputFileName = scanner.nextLine();

            System.out.println("Введіть ім'я вихідного файлу:");
            String outputFileName = scanner.nextLine();

            System.out.println("Виберіть розмір блоку:");
            System.out.println("1. 64 біти (2 гілки по 32 біти)");
            System.out.println("2. 128 біт (4 гілки по 32 біти)");
            int blockSizeChoice = Integer.parseInt(scanner.nextLine());
            int blockSize = (blockSizeChoice == 1) ? 64 : 128;

            System.out.println("Введіть кількість раундів (8-32):");
            int rounds = Integer.parseInt(scanner.nextLine());

            if (rounds < 8 || rounds > 32) {
                System.out.println("Кількість раундів повинна бути від 8 до 32. Встановлено 16 раундів.");
                rounds = 16;
            }

            System.out.println("Генерація ключа за допомогою алгоритму BBS...");
            System.out.println("Введіть перше просте число p (рекомендується >100):");
            long p = Long.parseLong(scanner.nextLine());

            System.out.println("Введіть друге просте число q (рекомендується >100):");
            long q = Long.parseLong(scanner.nextLine());

            System.out.println("Введіть початкове значення x0 (взаємно просте з p*q):");
            long x0 = Long.parseLong(scanner.nextLine());

            // Генеруємо 64-бітовий ключ за допомогою BBS
            long key = keyGeneratorService.generateBBSKey(p, q, x0, 64);
            System.out.println("Згенерований ключ (десяткове представлення): " + key);
            System.out.println("Ключ (шістнадцяткове представлення): " + Long.toHexString(key));

            // Якщо вхідний файл порожній або не існує, використовуємо прізвище та ім'я
            byte[] inputData;
            if (!fileService.fileExists(inputFileName) || fileService.isFileEmpty(inputFileName)) {
                System.out.println("Використовуємо стандартні дані: \"Popov Kyrylo\"");
                inputData = "Popov Kyrylo".getBytes(StandardCharsets.UTF_8);
            } else {
                inputData = fileService.readFromFile(inputFileName);
                System.out.println("Дані з файлу прочитані. Розмір: " + inputData.length + " байт");
            }

            byte[] outputData;
            if (choice == 1) {
                // Шифрування
                outputData = cipherService.encrypt(inputData, key, blockSize, rounds);
                System.out.println("Шифрування виконано успішно!");
            } else {
                // Дешифрування
                outputData = cipherService.decrypt(inputData, key, blockSize, rounds);
                System.out.println("Дешифрування виконано успішно!");
            }

            fileService.writeToFile(outputFileName, outputData);
            System.out.println("Результат записано у файл: " + outputFileName);

            // Вивід шістнадцяткового вигляду перших 16 байт результату
            StringBuilder hexOutput = new StringBuilder();
            for (int i = 0; i < Math.min(16, outputData.length); i++) {
                hexOutput.append(String.format("%02X ", outputData[i]));
            }
            System.out.println("Перші 16 байт результату (HEX): " + hexOutput);
        } catch (Exception e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
