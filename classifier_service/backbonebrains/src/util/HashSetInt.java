package util;

public class HashSetInt {

    LinkedListInt [] lists;

    public HashSetInt () {

        lists = new LinkedListInt[4];

        for (int index = 0; index < lists.length; index++) {

            lists[index] = new LinkedListInt();
        }
    }

    public void add (int value) {

        LinkedListInt list = lists[key_value(value)];

        if (list.contains(value) == -1) {

            list.addLast(value);
        }

        if (list.size() > lists.length) {


        }
    }

    public int contains (int value) {

        LinkedListInt list = lists[key_value(value)];

        return list.contains(value);
    }

    public void delete (int value) {

        LinkedListInt list = lists[key_value(value)];

        list.get(list.contains(value));
    }

    private int key_value (int value) {

        return value % lists.length;
    }

    private void refactor () {

        LinkedListInt [] oldLists = lists;

        lists = new LinkedListInt[oldLists.length * 2];

        for (LinkedListInt list: oldLists) {

            for (Integer value: list) {

                this.add(value);
            }
        }
    }

    public void print() {

        System.out.print("[ ");

        for (LinkedListInt list: lists) {

            for (Integer value: list) {

                System.out.print(value);
                System.out.print(" ");
            }
        }

        System.out.print("]");
    }
}

/*public class HashSetInt {

    LinkedListInt list;

    public HashSetInt () {

        list = new LinkedListInt();
    }

    public void add (int value) {

        if (list.contains(value) == -1) {

            list.addLast(value);
        }
    }

    public int contains (int value) {

        return list.contains(value);
    }

    public void delete (int value) {

            list.get(list.contains(value));
    }

    public void print() {

        list.printList();
    }
}*/
