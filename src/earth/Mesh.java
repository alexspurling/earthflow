package earth;

import java.util.List;

public interface Mesh {

    public void update(double dt);

    public List<Triangle> getTriangles();
}
