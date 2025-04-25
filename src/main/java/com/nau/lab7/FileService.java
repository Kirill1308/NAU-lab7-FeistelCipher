package com.nau.lab7;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileService {

    /**
     * Читає вміст файлу у вигляді масиву байтів
     *
     * @param fileName Ім'я файлу для читання
     * @return Вміст файлу як масив байтів
     * @throws IOException Якщо виникає помилка при читанні файлу
     */
    public byte[] readFromFile(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] data = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }

        return data;
    }

    /**
     * Записує масив байтів у файл
     *
     * @param fileName Ім'я файлу для запису
     * @param data Масив байтів для запису у файл
     * @throws IOException Якщо виникає помилка при записі у файл
     */
    public void writeToFile(String fileName, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
            fos.flush();
        }
    }

    /**
     * Перевіряє існування файлу
     *
     * @param fileName Ім'я файлу для перевірки
     * @return true, якщо файл існує, false - інакше
     */
    public boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.isFile();
    }

    /**
     * Перевіряє, чи порожній файл
     *
     * @param fileName Ім'я файлу для перевірки
     * @return true, якщо файл порожній або не існує, false - інакше
     */
    public boolean isFileEmpty(String fileName) {
        File file = new File(fileName);
        return !file.exists() || file.length() == 0;
    }
}
