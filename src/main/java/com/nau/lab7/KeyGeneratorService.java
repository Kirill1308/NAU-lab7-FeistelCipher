package com.nau.lab7;

import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class KeyGeneratorService {

    /**
     * Генерує ключ заданої бітової довжини за допомогою алгоритму Блюм-Блюм-Шуба (BBS)
     *
     * @param p Перше просте число
     * @param q Друге просте число
     * @param x0 Початкове значення (взаємно просте з p*q)
     * @param bitLength Кількість біт у ключі (зазвичай 64)
     * @return Згенерований ключ
     */
    public long generateBBSKey(long p, long q, long x0, int bitLength) {
        BigInteger m = BigInteger.valueOf(p).multiply(BigInteger.valueOf(q));
        BigInteger x = BigInteger.valueOf(x0);
        BigInteger TWO = BigInteger.valueOf(2);

        // Переконаємось, що x0 взаємно просте з m
        BigInteger gcd = x.gcd(m);
        if (!gcd.equals(BigInteger.ONE)) {
            // Якщо не взаємно просте, знайдемо взаємно просте
            x = x.add(BigInteger.ONE);
            gcd = x.gcd(m);
            while (!gcd.equals(BigInteger.ONE)) {
                x = x.add(BigInteger.ONE);
                gcd = x.gcd(m);
            }
        }

        // Генеруємо бітову послідовність потрібної довжини
        long key = 0;
        for (int i = 0; i < bitLength; i++) {
            // Генеруємо наступне значення за алгоритмом BBS
            x = x.modPow(TWO, m);

            // Беремо останній біт числа
            int bit = x.mod(TWO).intValue();

            // Додаємо біт до ключа
            key = (key << 1) | bit;
        }

        return key;
    }

    /**
     * Перевіряє, чи є число простим
     *
     * @param n Число для перевірки
     * @return true, якщо число просте, false - інакше
     */
    public boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }

        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Знаходить наступне просте число після заданого
     *
     * @param n Початкове число
     * @return Наступне просте число
     */
    public long nextPrime(long n) {
        if (n <= 1) {
            return 2;
        }

        long prime = n;
        boolean found = false;

        while (!found) {
            prime++;
            if (isPrime(prime)) {
                found = true;
            }
        }

        return prime;
    }
}
