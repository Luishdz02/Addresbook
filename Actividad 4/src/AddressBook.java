import java.io.*;
import java.util.*;
import java.util.regex.*;

public class AddressBook {
    private HashMap<String, Contact> contacts;
    private static final String FILE_NAME = "contacts_v2.dat";
    private Scanner scanner;

    // Clase interna para almacenar más información de contacto
    private class Contact implements Serializable {
        String name;
        String email;
        String address;
        String additionalNotes;

        public Contact(String name, String email, String address, String notes) {
            this.name = name;
            this.email = email;
            this.address = address;
            this.additionalNotes = notes;
        }

        @Override
        public String toString() {
            return String.format("Nombre: %s\nEmail: %s\nDirección: %s\nNotas: %s",
                    name, email, address, additionalNotes);
        }
    }

    public AddressBook() {
        contacts = new HashMap<>();
        scanner = new Scanner(System.in);
        load();
    }

    // Mejora para cargar contactos (ahora usa serialización)
    @SuppressWarnings("unchecked")
    public void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            contacts = (HashMap<String, Contact>) ois.readObject();
            System.out.println("Contactos cargados exitosamente.");
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo de contactos. Se creará uno nuevo.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al cargar contactos: " + e.getMessage());
        }
    }

    // Mejora para guardar contactos (serialización)
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(contacts);
            System.out.println("Contactos guardados exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar los contactos: " + e.getMessage());
        }
    }

    // Mejora para listar contactos con paginación
    public void list(int page, int pageSize) {
        if (contacts.isEmpty()) {
            System.out.println("No hay contactos en la agenda.");
            return;
        }

        List<String> phones = new ArrayList<>(contacts.keySet());
        Collections.sort(phones);

        int totalPages = (int) Math.ceil((double) phones.size() / pageSize);
        if (page < 1 || page > totalPages) {
            System.out.println("Página no válida.");
            return;
        }

        System.out.println("\n=== CONTACTOS (Página " + page + " de " + totalPages + ") ===");
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, phones.size());

        for (int i = start; i < end; i++) {
            String phone = phones.get(i);
            System.out.println("Teléfono: " + phone);
            System.out.println(contacts.get(phone));
            System.out.println("---------------------");
        }
    }

    // Mejora para crear contacto con validación
    public void create() {
        System.out.println("\n=== NUEVO CONTACTO ===");

        String phone;
        do {
            System.out.print("Teléfono (10 dígitos): ");
            phone = scanner.nextLine();
        } while (!isValidPhone(phone));

        if (contacts.containsKey(phone)) {
            System.out.println("¡Este número ya existe! Mostrando contacto existente:");
            System.out.println(contacts.get(phone));
            System.out.print("¿Desea actualizarlo? (s/n): ");
            if (!scanner.nextLine().equalsIgnoreCase("s")) {
                return;
            }
        }

        System.out.print("Nombre completo: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Dirección: ");
        String address = scanner.nextLine();

        System.out.print("Notas adicionales: ");
        String notes = scanner.nextLine();

        contacts.put(phone, new Contact(name, email, address, notes));
        System.out.println("Contacto " + (contacts.containsKey(phone) ? "actualizado" : "agregado") + " exitosamente.");
    }

    // Método para buscar contactos
    public void search() {
        System.out.println("\n=== BUSCAR CONTACTOS ===");
        System.out.println("1. Por nombre");
        System.out.println("2. Por teléfono");
        System.out.println("3. Por email");
        System.out.print("Seleccione opción: ");

        int option = Integer.parseInt(scanner.nextLine());
        System.out.print("Ingrese término de búsqueda: ");
        String term = scanner.nextLine().toLowerCase();

        boolean found = false;

        for (Map.Entry<String, Contact> entry : contacts.entrySet()) {
            Contact contact = entry.getValue();
            boolean match = false;

            switch (option) {
                case 1:
                    match = contact.name.toLowerCase().contains(term);
                    break;
                case 2:
                    match = entry.getKey().contains(term);
                    break;
                case 3:
                    match = contact.email.toLowerCase().contains(term);
                    break;
                default:
                    System.out.println("Opción no válida.");
                    return;
            }

            if (match) {
                System.out.println("\nTeléfono: " + entry.getKey());
                System.out.println(contact);
                System.out.println("---------------------");
                found = true;
            }
        }

        if (!found) {
            System.out.println("No se encontraron contactos que coincidan.");
        }
    }

    // Método para eliminar contacto con confirmación
    public void delete() {
        System.out.print("\nIngrese el número telefónico a eliminar: ");
        String phone = scanner.nextLine();

        if (!contacts.containsKey(phone)) {
            System.out.println("No se encontró el número en la agenda.");
            return;
        }

        System.out.println("\nContacto a eliminar:");
        System.out.println("Teléfono: " + phone);
        System.out.println(contacts.get(phone));
        System.out.print("¿Está seguro que desea eliminar este contacto? (s/n): ");

        if (scanner.nextLine().equalsIgnoreCase("s")) {
            contacts.remove(phone);
            System.out.println("Contacto eliminado exitosamente.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // Método para exportar contactos a CSV
    public void exportToCSV() {
        System.out.print("\nIngrese nombre del archivo CSV (sin extensión): ");
        String fileName = scanner.nextLine() + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Teléfono,Nombre,Email,Dirección,Notas");

            for (Map.Entry<String, Contact> entry : contacts.entrySet()) {
                Contact c = entry.getValue();
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        entry.getKey(), c.name, c.email, c.address, c.additionalNotes);
            }

            System.out.println("Contactos exportados exitosamente a " + fileName);
        } catch (IOException e) {
            System.out.println("Error al exportar: " + e.getMessage());
        }
    }

    // Método para mostrar menú mas interactivo
    public void showMenu() {
        int option;
        int currentPage = 1;
        final int PAGE_SIZE = 5;

        do {
            System.out.println("\n=== AGENDA TELEFÓNICA ===");
            System.out.println("1. Ver contactos (paginado)");
            System.out.println("2. Agregar/actualizar contacto");
            System.out.println("3. Buscar contactos");
            System.out.println("4. Eliminar contacto");
            System.out.println("5. Exportar a CSV");
            System.out.println("6. Siguiente página");
            System.out.println("7. Página anterior");
            System.out.println("8. Guardar y salir");
            System.out.print("Seleccione una opción: ");

            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    list(currentPage, PAGE_SIZE);
                    break;
                case 2:
                    create();
                    break;
                case 3:
                    search();
                    break;
                case 4:
                    delete();
                    break;
                case 5:
                    exportToCSV();
                    break;
                case 6:
                    currentPage++;
                    list(currentPage, PAGE_SIZE);
                    break;
                case 7:
                    currentPage = Math.max(1, currentPage - 1);
                    list(currentPage, PAGE_SIZE);
                    break;
                case 8:
                    save();
                    System.out.println("Saliendo de la aplicación...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (option != 8);

        scanner.close();
    }

    // Validación de número telefónico
    private boolean isValidPhone(String phone) {
        Pattern pattern = Pattern.compile("^\\d{10}$");
        if (!pattern.matcher(phone).matches()) {
            System.out.println("Formato inválido. Debe ser 10 dígitos.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        AddressBook addressBook = new AddressBook();
        addressBook.showMenu();
    }
}