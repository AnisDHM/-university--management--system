package model.dao;
import java.util.List;
import java.util.ArrayList;

import java.io.*;


public class FileStorage {
    private static final String DATA_FOLDER = "data/";
    
    //Méthode : saveToFile : elle peut sauvegarder une liste de n'importe quel type (Student , Teacher , Grade ..)
    /*  elle récupère
        objects --> liste des objets à sauvegarder
        filename --> nom du fichier où sauvegarder les objets 
    */
    public static <T> void saveToFile(List <T> objects , String filename){
        try(ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(DATA_FOLDER + filename) )){
            oos.writeObject(objects);
            System.out.println("Data saved to " + filename);
        } catch(IOException e){
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    /* La Méthode : loadFromFile
     * charge un fichier et retourn List<T>
     * si le fichier n'existe pas --> retourn une List vide
    */
    public static <T> List<T> loadFromFile(String filename){
        try(ObjectInputStream ois = new ObjectInputStream( new FileInputStream(DATA_FOLDER + filename))){
            return (List<T>) ois.readObject();
        
        }catch(FileNotFoundException e ){
            System.out.println("File not found: " + filename + ". Returning empty list.");
            return new ArrayList<>();
        } catch(IOException | ClassNotFoundException e){
            System.err.println("Error loading data: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
