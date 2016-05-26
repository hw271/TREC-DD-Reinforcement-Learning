import java.io.*;
import java.util.*;

public interface Model{
	public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException;
}
