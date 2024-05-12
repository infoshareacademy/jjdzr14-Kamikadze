package org.infoshere;

import org.infoshere.service.MenuService;

import java.io.IOException;


public class App {


    public static void main(String[] args) throws IOException {
        menu();
    }

    private static void menu() throws IOException {
        MenuService menuService = new MenuService();

        int optionNumber;

        /*
            klasa Menu
                print()
                selectOption(int i)
            klasa MainMenu extend Menu
                print() {
                   printMenu();
                   switch() {
                      case 1 -> printFunction1();

            klasa Function1Menu() extends Menu
                 print()
         */

        do {
            menuService.displayMenu();
            optionNumber = menuService.getChoice();
            switch (optionNumber) {
                case 1:
                    menuService.contentList("Trainers");
                    break;
                case 2:
                    menuService.contentList("ActivityList");
                    break;
                default:
                    if (optionNumber > 3) {
                        System.out.println("------------------------------------");
                        System.out.println("Off the scale. Please enter a valid number.");
                        System.out.println("------------------------------------");
                    }
            }
        }
        while (optionNumber != 3);
        menuService.exit();
    }


}
