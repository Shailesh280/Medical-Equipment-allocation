import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MedicalEquipmentAllocation {

    static class Location {
        double x, y;
        String address;

        public Location(double x, double y, String address) {
            this.x = x;
            this.y = y;
            this.address = address;
        }
    }

    public static double calculateDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.x - loc2.x, 2) + Math.pow(loc1.y - loc2.y, 2));
    }

    public static int findNearestLocation(int currentLocation, List<Location> locations, boolean[] visited) {
        int nearestLocation = -1;
        double shortestDistance = Double.MAX_VALUE;

        for (int i = 0; i < locations.size(); i++) {
            if (!visited[i]) {
                double distance = calculateDistance(locations.get(currentLocation), locations.get(i));
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestLocation = i;
                }
            }
        }
        return nearestLocation;
    }

    public static List<Integer> solveTSP(List<Location> locations) {
        int numLocations = locations.size();
        boolean[] visited = new boolean[numLocations];
        List<Integer> tour = new ArrayList<>();

        int currentLocation = 0; // Start from the first location
        tour.add(currentLocation);
        visited[currentLocation] = true;

        for (int i = 1; i < numLocations; i++) {
            int nearestLocation = findNearestLocation(currentLocation, locations, visited);
            tour.add(nearestLocation);
            visited[nearestLocation] = true;
            currentLocation = nearestLocation;
        }

        return tour;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Medical Equipment Allocation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.setLayout(new BorderLayout());

            // Title
            JLabel titleLabel = new JLabel("Medical Equipment Allocation", JLabel.CENTER);
            titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
            frame.add(titleLabel, BorderLayout.NORTH);

            // Create a split pane to divide the window into two halves
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(500); // Initial position of the divider
            splitPane.setResizeWeight(0.5); // 50/50 split

            // Left panel for input and output
            JPanel leftPanel = new JPanel(new BorderLayout());

            // Input Panel
            JPanel inputPanel = new JPanel();
            inputPanel.setBorder(BorderFactory.createTitledBorder("Add Location"));
            inputPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0;
            gbc.gridy = 0;
            inputPanel.add(new JLabel("Address:"), gbc);
            JTextField addressField = new JTextField(20);
            gbc.gridx = 1;
            inputPanel.add(addressField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            inputPanel.add(new JLabel("X Coordinate:"), gbc);
            JTextField xField = new JTextField(10);
            gbc.gridx = 1;
            inputPanel.add(xField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            inputPanel.add(new JLabel("Y Coordinate:"), gbc);
            JTextField yField = new JTextField(10);
            gbc.gridx = 1;
            inputPanel.add(yField, gbc);

            JButton addButton = new JButton("Add Location");
            gbc.gridx = 1;
            gbc.gridy = 3;
            inputPanel.add(addButton, gbc);

            leftPanel.add(inputPanel, BorderLayout.NORTH);

            // Output Panel
            JPanel outputPanel = new JPanel();
            outputPanel.setBorder(BorderFactory.createTitledBorder("Route Calculation"));
            outputPanel.setLayout(new BorderLayout());

            JTextArea outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(outputArea);
            outputPanel.add(scrollPane, BorderLayout.CENTER);

            JButton calculateButton = new JButton("Calculate Route");
            outputPanel.add(calculateButton, BorderLayout.SOUTH);

            leftPanel.add(outputPanel, BorderLayout.CENTER);

            splitPane.setLeftComponent(leftPanel);

            // Right panel for route visualization
            RoutePanel routePanel = new RoutePanel();
            routePanel.setBorder(BorderFactory.createTitledBorder("Route Visualization"));
            splitPane.setRightComponent(routePanel);

            frame.add(splitPane, BorderLayout.CENTER);

            // Data storage
            List<Location> locations = new ArrayList<>();

            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String address = addressField.getText();
                        double x = Double.parseDouble(xField.getText());
                        double y = Double.parseDouble(yField.getText());
                        locations.add(new Location(x, y, address));
                        outputArea.append("Added: " + address + " (" + x + ", " + y + ")\n");
                        addressField.setText("");
                        xField.setText("");
                        yField.setText("");
                        routePanel.setLocations(locations);
                        routePanel.setTour(null); // Reset tour for new input
                        routePanel.repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid input. Please enter numeric values for coordinates.");
                    }
                }
            });

            calculateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (locations.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "No locations added.");
                        return;
                    }

                    List<Integer> tour = solveTSP(locations);
                    double totalDistance = 0;
                    outputArea.setText("");
                    outputArea.append("Order of locations to visit:\n");
                    for (int i = 0; i < tour.size(); i++) {
                        int loc = tour.get(i);
                        outputArea.append((i + 1) + ". " + locations.get(loc).address + "\n");
                        if (i > 0) {
                            totalDistance += calculateDistance(locations.get(tour.get(i - 1)), locations.get(loc));
                        }
                    }
                    outputArea.append("Total distance: " + totalDistance + "\n");

                    // Update and repaint route panel
                    routePanel.setLocations(locations);
                    routePanel.setTour(tour);
                    routePanel.repaint();

                    // Do not clear input fields for next use
                }
            });

            frame.setVisible(true);
        });
    }
}

class RoutePanel extends JPanel {
    private List<MedicalEquipmentAllocation.Location> locations;
    private List<Integer> tour;

    public void setLocations(List<MedicalEquipmentAllocation.Location> locations) {
        this.locations = locations;
    }

    public void setTour(List<Integer> tour) {
        this.tour = tour;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (locations == null || tour == null) {
            return;
        }

        // Determine bounds
        double minX = locations.stream().mapToDouble(loc -> loc.x).min().orElse(0);
        double minY = locations.stream().mapToDouble(loc -> loc.y).min().orElse(0);
        double maxX = locations.stream().mapToDouble(loc -> loc.x).max().orElse(1);
        double maxY = locations.stream().mapToDouble(loc -> loc.y).max().orElse(1);

        int padding = 50;
        int width = getWidth() - 2 * padding;
        int height = getHeight() - 2 * padding;

        // Draw locations
        g2d.setColor(Color.BLUE);
        for (MedicalEquipmentAllocation.Location loc : locations) {
            int x = padding + (int) ((loc.x - minX) / (maxX - minX) * width);
            int y = padding + (int) ((loc.y - minY) / (maxY - minY) * height);
            g2d.fillOval(x - 5, y - 5, 10, 10);
            g2d.drawString(loc.address, x + 10, y - 10);
        }

        // Draw route
        g2d.setColor(Color.RED);
        for (int i = 1; i < tour.size(); i++) {
            MedicalEquipmentAllocation.Location loc1 = locations.get(tour.get(i - 1));
            MedicalEquipmentAllocation.Location loc2 = locations.get(tour.get(i));
            int x1 = padding + (int) ((loc1.x - minX) / (maxX - minX) * width);
            int y1 = padding + (int) ((loc1.y - minY) / (maxY - minY) * height);
            int x2 = padding + (int) ((loc2.x - minX) / (maxX - minX) * width);
            int y2 = padding + (int) ((loc2.y - minY) / (maxY - minY) * height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw closing line (from last location back to first location)
        if (!tour.isEmpty()) {
            MedicalEquipmentAllocation.Location firstLoc = locations.get(tour.get(0));
            MedicalEquipmentAllocation.Location lastLoc = locations.get(tour.get(tour.size() - 1));
            int x1 = padding + (int) ((lastLoc.x - minX) / (maxX - minX) * width);
            int y1 = padding + (int) ((lastLoc.y - minY) / (maxY - minY) * height);
            int x2 = padding + (int) ((firstLoc.x - minX) / (maxX - minX) * width);
            int y2 = padding + (int) ((firstLoc.y - minY) / (maxY - minY) * height);
            g2d.setColor(Color.GREEN); // Change color for the closing line
            g2d.drawLine(x1, y1, x2, y2);
            }
            }
            }
