package net.dandielo.core.items.serialize.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.dandielo.core.items.dItem;
import net.dandielo.core.items.serialize.Attribute;
import net.dandielo.core.items.serialize.ItemAttribute;
import net.dandielo.core.utils.NBTItemStack;

@Attribute(key="bk", name="Book", priority = 45, items = {Material.BOOK_AND_QUILL, Material.WRITTEN_BOOK})
public class Book extends ItemAttribute {
	//book id used for loading and saving
	private String bookId;
	
	//for WrittenBook
	private String author;
	private String title;
	private int generation;
	
	//for each book item
	private List<String> pages;
	
	public Book(dItem item, String key)
	{
		super(item, key);
		
		//set defaults
		author = null;
		title = null;
		generation = 0;
		
		//empty pages list
		pages = new ArrayList<String>();
	}

	@Override
	public boolean deserialize(String data) 
	{
		//get the saved book id (used to retrieve data from books.yml)
		bookId = data;
		
		//load the book from file
		author = books.getString(bookId + ".author");
		title = books.getString(bookId + ".title");
		generation = books.getInt(bookId + ".generation");
		pages.addAll(books.getStringList(bookId + ".pages"));
		return true;
	}

	@Override
	public String serialize()
	{
		//save the book
		books.set(bookId + ".author", author);
		books.set(bookId + ".title", title);
		books.set(bookId + ".pages", pages);
		books.set(bookId + ".generation", generation);
		
		//save the file
		save();
		
		//return the books id
		return bookId;
	}

	@Override
	public ItemStack onNativeAssign(ItemStack item, boolean unused) 
	{
		if ( !(item.getItemMeta() instanceof BookMeta) ) return item;
		BookMeta book = (BookMeta) item.getItemMeta();
		
		//for written books set the title and author
		if ( item.getType().equals(Material.WRITTEN_BOOK) )
		{
			book.setAuthor(author);
			book.setTitle(title);
		}
		
		// set all pages and the item meta
		book.setPages(pages);
		item.setItemMeta(book);
		
		// set the book generation and return the new item
		NBTItemStack helper = new NBTItemStack(item);
		helper.setInt("generation", generation);
		return helper.getItemStack();
	}

	@Override
	public boolean onRefactor(ItemStack item)
	{
		if ( !(item.getItemMeta() instanceof BookMeta) ) return false;
		
		//get the book information
		BookMeta book = (BookMeta) item.getItemMeta();
		
		//get title and author
		author = book.getAuthor();
		title = book.getTitle();
		
		// Get the books generation
		NBTItemStack helper = new NBTItemStack(item);
		generation = helper.getInt("generation");
		
		//get pages
		pages.addAll(book.getPages());
		
		//generate an Id (fixed a null ptr when no title is set)
		bookId = (title != null ? title.replace(" ", "_") : "bookAndQuil" + new Random().nextInt(100)) + new Random().nextInt(1000);
		return true;
	}
	
	/**
	 * Static constructor
	 */
	static
	{
		try
		{
			loadBooks();
		}
		catch( Exception e )
		{
			//TODO: debugger dB.high("Loading books failed");
		}
	}
	
	/**
	 * The yaml configuration that stores all books 
	 */
	private static FileConfiguration books;
	private static File booksFile;
	
	/**
	 * YamlStorageFile loading
	 * @throws Exception 
	 */
	public static void loadBooks() throws Exception
	{
		String fileName = "books.yml";
		String filePath = "plugins/dtlTraders";
		
		//check the base directory
		File baseDirectory = new File(filePath);
		if ( !baseDirectory.exists() ) 
			baseDirectory.mkdirs();
		
		booksFile = new File(filePath, fileName);
		//if the file does not exists
		if ( !booksFile.exists() )
		{
			//create the file
			booksFile.createNewFile();
		}
		
		//create the books file configuration
		books = new YamlConfiguration();
		
		//load the file as yaml
		books.load(booksFile);
	}
	
	public static void save()
	{
		try
		{
			books.save(booksFile);
		}
		catch( IOException e )
		{
			//TODO: debugger dB.high("Could not save the books file");
		}
	}
	
	@Override
	public boolean similar(ItemAttribute that)
	{
		return same(that);
	}
	
	@Override
	public boolean same(ItemAttribute thato)
	{
		if (!(thato instanceof Book)) return false;
		Book that = (Book) thato;
		

		boolean result = true;
		//check author and title
		result &= author == null ? that.author == null : author.equals(that.author);
		result &= title == null ? that.title == null : title.equals(that.title);
		result &= generation == that.generation;
		
		//check each page
		result &= pages.size() == that.pages.size();
		for (int i = 0; i < pages.size() && result; ++i)
			result &= pages.get(i).equals(that.pages.get(i));
		
		//return the result
		return result;
	}
}
