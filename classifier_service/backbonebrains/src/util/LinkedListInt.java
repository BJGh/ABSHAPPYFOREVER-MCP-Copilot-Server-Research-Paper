package util;

import java.util.Iterator;

public class LinkedListInt implements Iterable<Integer> {

    private static class Element {

        int value;
        Element next;
        Element previous;

        Element (int value, Element next, Element previous) {

            this.value = value;
            this.next = next;
            this.previous = previous;
        }

        Element (int value) {

            this(value, null, null);
        }
    }

    private Element firstElement;
    private Element lastElement;

    public LinkedListInt() {

        firstElement = null;
        lastElement = null;
    }

    // Добавить элемент в начало списка
    public void addFirst (int value) {

        if (firstElement == null) {

            firstElement = lastElement = new Element(value);
        } else {

            Element newElement = new Element(value, firstElement, null);
            firstElement.previous = newElement;
            firstElement = newElement;
        }
    }
    // Добавить элемент в конец списка
    public void addLast (int value) {

        if (lastElement == null) {

            firstElement = lastElement = new Element(value);
        } else {

            Element newElement = new Element(value, null, lastElement);
            lastElement.next = newElement;
            lastElement = newElement;
        }
    }
    // Вставить элемент по индексу
    public void add (int index, int value) {

        Element element;
        for (element = firstElement; element != null && index != 0; element = element.next, index--);


        if (element == firstElement)
            addFirst(value);
        else if (element == null && firstElement != null && index == 0)
            addLast(value);
        else {

            Element newElement = new Element(value, element, element.previous);
            element.previous.next = newElement;
            element.previous = newElement;
        }
    }

    // Посмотреть первый элемент списка
    public  int peekFirst () {

        return firstElement.value;
    }
    // Посмотреть последний элемнт списка
    public  int peekLast () {

        return lastElement.value;
    }
    // Посмотреть элемнт списка
    public int peek (int index) {

        Element element;
        for (element = firstElement; element != lastElement && index != 0; element = element.next, index--);

        try {

            return element.value;
        } finally {

        }
    }

    // Извлекает первый элемент из списка
    public int getFirst () {

        try {

            return firstElement.value;
        } finally {

            if (firstElement != lastElement) {
                firstElement = firstElement.next;
                firstElement.previous = null;
            } else
                firstElement = lastElement = null;
        }
    }
    // Извлекает последний элемент списка
    public int getLast () {

        try {

            return lastElement.value;
        } finally {

            if (lastElement != firstElement) {
                lastElement = lastElement.previous;
                lastElement.next = null;
            } else
                firstElement = lastElement = null;
        }
    }
    // Извлечь элемент по индексу
    public int get (int index) {

        Element element;
        for (element = firstElement; element != lastElement && index != 0; element = element.next, index--);

        try {

            return element.value;
        } finally {

            if (element == firstElement)
                getFirst();
            else if (element == lastElement && index == 0)
                getLast();
            else {
                element.previous.next = element.next;
                element.next.previous = element.previous;
            }
        }
    }

    // Вывод списка
    public void printList () {

        System.out.print("[");

        for (Element element = firstElement; element != null; element = element.next) {
            System.out.print(element.value);
            if (element.next != null)
                System.out.print(", ");
            else
                System.out.print("]");

        }
    }

    public int contains (int value) {

        int index = 0;

        for (Element element = firstElement; element != null; element = element.next, index++) {

            if (element.value == value)
                return index;
        }

        return -1;
    }

    public int size () {

        int size = 0;

        for (Element element = firstElement; element != null; element = element.next, size++);

        return size;
    }

    // Реализация итератора
    @Override
    public Iterator iterator() {

        return new Iterator() {

            Element nextElement = firstElement;

            @Override
            public boolean hasNext() {

                return nextElement != null;
            }

            @Override
            public Integer next() {

                try {

                    return nextElement.value;
                } finally {

                    nextElement = nextElement.next;
                }
            }
        };
    }
}
