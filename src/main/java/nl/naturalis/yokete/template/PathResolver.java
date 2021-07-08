package nl.naturalis.yokete.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface PathResolver {

  Optional<Boolean> isValidPath(String path);

  InputStream resolvePath(String path) throws IOException;
}
