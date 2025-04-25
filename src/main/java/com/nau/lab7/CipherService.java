package com.nau.lab7;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CipherService {

    // Функція шифрування
    public byte[] encrypt(byte[] data, long key, int blockSize, int rounds) {
        // Доповнюємо дані, якщо їх розмір не кратний blockSize/8
        data = padData(data, blockSize / 8);

        byte[] result = new byte[data.length];

        // Розбиваємо на блоки і шифруємо кожен
        for (int i = 0; i < data.length; i += blockSize / 8) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize / 8);
            byte[] encryptedBlock = processBlock(block, key, blockSize, rounds, true);
            System.arraycopy(encryptedBlock, 0, result, i, blockSize / 8);
        }

        return result;
    }

    // Функція дешифрування
    public byte[] decrypt(byte[] data, long key, int blockSize, int rounds) {
        byte[] result = new byte[data.length];

        // Розбиваємо на блоки і дешифруємо кожен
        for (int i = 0; i < data.length; i += blockSize / 8) {
            byte[] block = Arrays.copyOfRange(data, i, i + blockSize / 8);
            byte[] decryptedBlock = processBlock(block, key, blockSize, rounds, false);
            System.arraycopy(decryptedBlock, 0, result, i, blockSize / 8);
        }

        // Видаляємо доповнення
        return unpadData(result);
    }

    // Обробка одного блоку
    private byte[] processBlock(byte[] block, long key, int blockSize, int rounds, boolean encrypt) {
        if (blockSize == 64) {
            return processFeistel64BitBlock(block, key, rounds, encrypt);
        } else {
            return processFeistel128BitBlock(block, key, rounds, encrypt);
        }
    }

    // Мережа Фейстеля для 64-бітного блоку (2 гілки по 32 біти)
    private byte[] processFeistel64BitBlock(byte[] block, long key, int rounds, boolean encrypt) {
        // Розбиваємо на ліву та праву частини
        int[] leftRight = bytesToInts(block);
        int left = leftRight[0];
        int right = leftRight[1];

        // Генеруємо раундові ключі
        int[] roundKeys = generateRoundKeys(key, rounds);

        // Якщо дешифрування, інвертуємо порядок ключів
        if (!encrypt) {
            reverseArray(roundKeys);
        }

        // Виконуємо раунди мережі Фейстеля
        for (int round = 0; round < rounds; round++) {
            int temp = right;
            right = left ^ feistelFunction(right, roundKeys[round]);
            left = temp;
        }

        // Збираємо результат
        return intsToBytes(right, left); // Зверніть увагу на зміну порядку правої і лівої частин
    }

    // Мережа Фейстеля для 128-бітного блоку (4 гілки по 32 біти)
    private byte[] processFeistel128BitBlock(byte[] block, long key, int rounds, boolean encrypt) {
        // Розбиваємо на 4 частини
        int[] parts = bytesToInts(block);
        int a = parts[0];
        int b = parts[1];
        int c = parts[2];
        int d = parts[3];

        // Генеруємо раундові ключі
        int[] roundKeys = generateRoundKeys(key, rounds);

        // Якщо дешифрування, інвертуємо порядок ключів
        if (!encrypt) {
            reverseArray(roundKeys);
        }

        // Виконуємо раунди мережі Фейстеля з 4 гілками
        for (int round = 0; round < rounds; round++) {
            int temp = a;
            a = d;
            d = c;
            c = b;
            b = temp ^ feistelFunction(d, roundKeys[round]);
        }

        // Збираємо результат
        return intsToBytes(a, b, c, d);
    }

    // Функція F мережі Фейстеля
    private int feistelFunction(int input, int roundKey) {
        // Розширюємо 32-бітний вхід до 48 біт шляхом перестановки та повторення
        long expanded = expand(input);

        // XOR з раундовим ключем
        expanded ^= roundKey;

        // Застосовуємо S-бокси (спрощений варіант)
        int result = substitution(expanded);

        // Перестановка бітів
        return permutation(result);
    }

    // Розширення 32-бітного значення до 48 біт
    private long expand(int input) {
        // Простий приклад розширення: копіюємо вхідне значення і додаємо біти
        long result = input & 0xFFFFFFFFL;
        // Додаємо 16 додаткових біт з різних частин вхідного значення
        result |= ((input & 0xF) << 32);
        result |= ((input & 0xF0) << 36);
        result |= ((input & 0xF00) << 40);
        result |= ((input & 0xF000) << 44);
        return result;
    }

    // Спрощена функція підстановки (S-бокси)
    private int substitution(long input) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int sboxInput = (int)((input >> (i * 6)) & 0x3F); // 6 біт для кожного S-боксу
            int sboxOutput = sBox(sboxInput, i);
            result |= (sboxOutput << (i * 4)); // 4-бітний вихід
        }
        return result;
    }

    // Спрощений S-бокс
    private int sBox(int input, int boxNumber) {
        // Тут можна реалізувати більш складні S-бокси
        return ((input * (boxNumber + 1)) ^ (input >> 1)) & 0xF;
    }

    // Перестановка бітів
    private int permutation(int input) {
        // Проста перестановка: циклічний зсув вправо
        return Integer.rotateRight(input, 8);
    }

    // Генерація раундових ключів
    private int[] generateRoundKeys(long key, int rounds) {
        int[] roundKeys = new int[rounds];

        for (int i = 0; i < rounds; i++) {
            // Простий алгоритм генерації раундових ключів
            // В реальному застосуванні бажано використовувати більш складний алгоритм
            roundKeys[i] = (int)(key ^ (key >> 32));
            key = Long.rotateLeft(key, 5) ^ (i + 1);
        }

        return roundKeys;
    }

    // Перетворення масиву байтів на масив int
    private int[] bytesToInts(byte[] bytes) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((bytes[i * 4] & 0xFF) << 24) |
                        ((bytes[i * 4 + 1] & 0xFF) << 16) |
                        ((bytes[i * 4 + 2] & 0xFF) << 8) |
                        (bytes[i * 4 + 3] & 0xFF);
        }
        return result;
    }

    // Перетворення двох int на масив байтів
    private byte[] intsToBytes(int a, int b) {
        byte[] result = new byte[8];

        result[0] = (byte)(a >> 24);
        result[1] = (byte)(a >> 16);
        result[2] = (byte)(a >> 8);
        result[3] = (byte)a;

        result[4] = (byte)(b >> 24);
        result[5] = (byte)(b >> 16);
        result[6] = (byte)(b >> 8);
        result[7] = (byte)b;

        return result;
    }

    // Перетворення чотирьох int на масив байтів
    private byte[] intsToBytes(int a, int b, int c, int d) {
        byte[] result = new byte[16];

        result[0] = (byte)(a >> 24);
        result[1] = (byte)(a >> 16);
        result[2] = (byte)(a >> 8);
        result[3] = (byte)a;

        result[4] = (byte)(b >> 24);
        result[5] = (byte)(b >> 16);
        result[6] = (byte)(b >> 8);
        result[7] = (byte)b;

        result[8] = (byte)(c >> 24);
        result[9] = (byte)(c >> 16);
        result[10] = (byte)(c >> 8);
        result[11] = (byte)c;

        result[12] = (byte)(d >> 24);
        result[13] = (byte)(d >> 16);
        result[14] = (byte)(d >> 8);
        result[15] = (byte)d;

        return result;
    }

    // Доповнення даних до розміру, кратного blockSizeBytes
    private byte[] padData(byte[] data, int blockSizeBytes) {
        int padding = blockSizeBytes - (data.length % blockSizeBytes);
        if (padding == blockSizeBytes) {
            padding = 0;
        }

        byte[] paddedData = Arrays.copyOf(data, data.length + padding);
        if (padding > 0) {
            // Заповнюємо останній байт значенням, що дорівнює кількості доданих байтів
            paddedData[paddedData.length - 1] = (byte)padding;
        }

        return paddedData;
    }

    // Видалення доповнення
    private byte[] unpadData(byte[] data) {
        int padding = data[data.length - 1] & 0xFF;
        if (padding > 0 && padding < 16) {
            return Arrays.copyOf(data, data.length - padding);
        }
        return data;
    }

    // Зміна порядку елементів у масиві
    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }
}
