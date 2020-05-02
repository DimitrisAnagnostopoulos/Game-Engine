package game;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import entities.*;
import entitySheets.*;

import java.awt.Canvas;

import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import java.awt.Font;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSlider;
import java.awt.BorderLayout;

public class Editor {

	private JFrame frame = new JFrame();
	private String[] entityTypes = { "person", "item", "scenery", "floor" };
	private String currentEntityType;
	private JTable propertyTable;
	private File selectedFile;
	private List<EntitySheet> entitySheetList = new ArrayList<EntitySheet>();
	private DefaultMutableTreeNode selectedTreeNode;
	private Entity selectedEntity;

	/*
	 * public static void main(String[] args) { EventQueue.invokeLater(new
	 * Runnable() { public void run() { try { Editor window = new Editor();
	 * window.frame.setVisible(true); } catch (Exception e) {
	 * e.printStackTrace(); } } }); }
	 */

	public Editor() {
		initialize();
	}

	private void initialize() {
		frame.setBounds(100, 100, 1800, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JSplitPane split = new JSplitPane();
		split.setDividerLocation(1280);
		frame.getContentPane().add(split);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		split.setRightComponent(tabbedPane);
		JPanel world_editor = new JPanel();
		tabbedPane.addTab("World editor", null, world_editor, null);

		JTree objects_tree = new JTree(createObjectTreeNodes(entitySheetList));
		objects_tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				selectedTreeNode = (DefaultMutableTreeNode) objects_tree.getLastSelectedPathComponent();
			}
		});

		JButton object_tree_refresh = new JButton("Refresh");

		object_tree_refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				generateEntitySheetList(entitySheetList);
				DefaultMutableTreeNode root = createObjectTreeNodes(entitySheetList);
				objects_tree.setModel(new DefaultTreeModel(root));
			}
		});

		JSeparator separator_1 = new JSeparator();

		JLabel lblPosition = new JLabel("Position");
		lblPosition.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JLabel lblX = new JLabel("X");
		lblX.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSpinner spinner_pos_x = new JSpinner();
		spinner_pos_x.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					Vector3f position = selectedEntity.getPosition();
					position.x = (float) spinner_pos_x.getValue();
					selectedEntity.setPosition(position);
				}
			}
		});
		spinner_pos_x.setModel(new SpinnerNumberModel(new Float(0), null, null, new Float(1)));

		JLabel lblY = new JLabel("Y");
		lblY.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSpinner spinner_pos_y = new JSpinner();
		spinner_pos_y.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					Vector3f position = selectedEntity.getPosition();
					position.y = (float) spinner_pos_y.getValue();
					selectedEntity.setPosition(position);
				}
			}
		});
		spinner_pos_y.setModel(new SpinnerNumberModel(new Float(0), null, null, new Float(1)));

		JLabel lblZ = new JLabel("Z");
		lblZ.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSpinner spinner_pos_z = new JSpinner();
		spinner_pos_z.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					Vector3f position = selectedEntity.getPosition();
					position.z = (float) spinner_pos_z.getValue();
					selectedEntity.setPosition(position);
				}
			}
		});
		spinner_pos_z.setModel(new SpinnerNumberModel(new Float(0), null, null, new Float(1)));

		JLabel lblRotation = new JLabel("Rotation");
		lblRotation.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JLabel lblX_1 = new JLabel("X");
		lblX_1.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSlider slider_rot_x = new JSlider();
		slider_rot_x.setPaintTicks(true);
		slider_rot_x.setMinorTickSpacing(15);
		slider_rot_x.setSnapToTicks(true);
		slider_rot_x.setMaximum(180);
		slider_rot_x.setValue(0);
		slider_rot_x.setMinimum(-180);
		slider_rot_x.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					selectedEntity.setRotX((float) slider_rot_x.getValue());
				}
			}
		});

		JLabel lblY_1 = new JLabel("Y");
		lblY_1.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSlider slider_rot_y = new JSlider();
		slider_rot_y.setPaintTicks(true);
		slider_rot_y.setSnapToTicks(true);
		slider_rot_y.setMinorTickSpacing(15);
		slider_rot_y.setMaximum(180);
		slider_rot_y.setValue(0);
		slider_rot_y.setMinimum(-180);
		slider_rot_y.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					selectedEntity.setRotY((float) slider_rot_y.getValue());
				}
			}
		});

		JLabel lblZ_1 = new JLabel("Z");
		lblZ_1.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSlider slider_rot_z = new JSlider();
		slider_rot_z.setSnapToTicks(true);
		slider_rot_z.setPaintTicks(true);
		slider_rot_z.setMinorTickSpacing(15);
		slider_rot_z.setMaximum(180);
		slider_rot_z.setValue(0);
		slider_rot_z.setMinimum(-180);
		slider_rot_z.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					selectedEntity.setRotZ((float) slider_rot_z.getValue());
				}
			}
		});

		JLabel lblScale = new JLabel("Scale");
		lblScale.setFont(new Font("Tahoma", Font.PLAIN, 22));

		JSlider slider_scale = new JSlider();
		slider_scale.setValue(100);
		slider_scale.setMaximum(200);
		slider_scale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (selectedEntity != null) {
					selectedEntity.setScale((float) (slider_scale.getValue() / 50f));
				}
			}
		});

		JSeparator separator_2 = new JSeparator();

		JButton add_to_scene_button = new JButton("Add object to scene");
		add_to_scene_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (selectedTreeNode.getChildCount() == 0) {
					for (EntitySheet entitySheet : entitySheetList) {
						if (entitySheet.name == selectedTreeNode.toString()) {
							Float pos_x = (Float) spinner_pos_x.getValue();
							Float pos_y = (Float) spinner_pos_y.getValue();
							Float pos_z = (Float) spinner_pos_z.getValue();
							Float rot_x = (float) slider_rot_x.getValue();
							Float rot_y = (float) slider_rot_y.getValue();
							Float rot_z = (float) slider_rot_z.getValue();
							Float scale = (float) (slider_scale.getValue() / 50f);
							Entity entity = null;
							switch (entitySheet.getClass().getName()) {
							case "entitySheets.PersonSheet":
								entity = new Person((PersonSheet) entitySheet, new Vector3f(pos_x, pos_y, pos_z), rot_x,
										rot_y, rot_z, scale);
								break;
							case "entitySheets.ItemSheet":
								entity = new Item((ItemSheet) entitySheet, new Vector3f(pos_x, pos_y, pos_z), rot_x,
										rot_y, rot_z, scale);
								break;
							case "entitySheets.ScenerySheet":
								entity = new Scenery((ScenerySheet) entitySheet, new Vector3f(pos_x, pos_y, pos_z),
										rot_x, rot_y, rot_z, scale);
								break;
							case "entitySheets.FloorSheet":
								entity = new Floor((FloorSheet) entitySheet, new Vector3f(pos_x, pos_y, pos_z), rot_x,
										rot_y, rot_z, scale);
								break;
							}
							selectedEntity = entity;
							Handler.addEntity(entity);
						}
					}
				}
			}
		});

		JButton save_room_button = new JButton("SAVE ROOM");
		save_room_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Handler.save("Room1");
			}
		});

		JButton load_room_button = new JButton("LOAD ROOM");
		load_room_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler.load("Room1");
			}
		});

		JSeparator separator = new JSeparator();

		JButton clear_room_button = new JButton("CLEAR ROOM");
		clear_room_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Handler.clearScene();
			}
		});

		GroupLayout gl_world_editor = new GroupLayout(world_editor);
		gl_world_editor.setHorizontalGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_world_editor.createSequentialGroup()
						.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_world_editor.createSequentialGroup().addGap(6)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(objects_tree, GroupLayout.PREFERRED_SIZE, 473,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(object_tree_refresh).addComponent(separator_1,
														GroupLayout.PREFERRED_SIZE, 473, GroupLayout.PREFERRED_SIZE)))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap()
										.addComponent(lblPosition))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap()
										.addComponent(lblRotation))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap().addComponent(lblX)
										.addGap(6)
										.addComponent(spinner_pos_x, GroupLayout.PREFERRED_SIZE, 99,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblY)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(spinner_pos_y, GroupLayout.PREFERRED_SIZE, 99,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblZ)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(spinner_pos_z,
												GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap()
										.addComponent(save_room_button).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(load_room_button).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(clear_room_button))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap().addComponent(
										separator, GroupLayout.PREFERRED_SIZE, 473, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap()
										.addComponent(add_to_scene_button))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap().addComponent(
										separator_2, GroupLayout.PREFERRED_SIZE, 473, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_world_editor.createSequentialGroup().addContainerGap()
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(lblY_1, GroupLayout.PREFERRED_SIZE, 13,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(lblScale).addComponent(lblZ_1).addComponent(lblX_1))
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(slider_rot_x, Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
												.addComponent(slider_scale, Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
												.addComponent(slider_rot_y, GroupLayout.DEFAULT_SIZE, 422,
														Short.MAX_VALUE)
												.addComponent(slider_rot_z, GroupLayout.DEFAULT_SIZE, 422,
														Short.MAX_VALUE))))
						.addContainerGap()));
		gl_world_editor
				.setVerticalGroup(
						gl_world_editor.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_world_editor.createSequentialGroup().addGap(6)
										.addComponent(objects_tree, GroupLayout.PREFERRED_SIZE, 300,
												GroupLayout.PREFERRED_SIZE)
										.addGap(6).addComponent(object_tree_refresh).addGap(6)
										.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblPosition)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(lblY).addGroup(gl_world_editor
														.createSequentialGroup().addGroup(gl_world_editor
																.createParallelGroup(Alignment.TRAILING, false)
																.addComponent(lblX, GroupLayout.DEFAULT_SIZE,
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(lblZ, GroupLayout.DEFAULT_SIZE,
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(spinner_pos_x, Alignment.LEADING,
																		GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
																.addComponent(spinner_pos_y, Alignment.LEADING)
																.addComponent(spinner_pos_z, Alignment.LEADING))
														.addPreferredGap(ComponentPlacement.UNRELATED)
														.addComponent(lblRotation)))
										.addGap(18)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(slider_rot_x, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblX_1))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(lblY_1).addComponent(slider_rot_y,
														GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(lblZ_1).addComponent(slider_rot_z,
														GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.LEADING)
												.addComponent(lblScale).addComponent(slider_scale,
														GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
										.addGap(248)
										.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(add_to_scene_button)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_world_editor.createParallelGroup(Alignment.BASELINE)
												.addComponent(save_room_button).addComponent(load_room_button)
												.addComponent(clear_room_button))
										.addGap(74)));
		world_editor.setLayout(gl_world_editor);

		JPanel object_editor = new JPanel();
		tabbedPane.addTab("Object editor", null, object_editor, null);
		SpringLayout sl_object_editor = new SpringLayout();
		object_editor.setLayout(sl_object_editor);

		JToolBar toolBar = new JToolBar();
		sl_object_editor.putConstraint(SpringLayout.NORTH, toolBar, 0, SpringLayout.NORTH, object_editor);
		sl_object_editor.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, object_editor);
		sl_object_editor.putConstraint(SpringLayout.EAST, toolBar, 1779, SpringLayout.WEST, object_editor);
		object_editor.add(toolBar);

		JButton create_entity_button = new JButton("Create Entity");
		toolBar.add(create_entity_button);

		JButton load_entity_button = new JButton("Load Entity");
		toolBar.add(load_entity_button);

		JButton save_entity_button = new JButton("Save Entity");
		toolBar.add(save_entity_button);

		JLabel object_name_label = new JLabel();
		sl_object_editor.putConstraint(SpringLayout.NORTH, object_name_label, 7, SpringLayout.SOUTH, toolBar);
		sl_object_editor.putConstraint(SpringLayout.WEST, object_name_label, 6, SpringLayout.WEST, object_editor);
		sl_object_editor.putConstraint(SpringLayout.EAST, object_name_label, -1334, SpringLayout.EAST, object_editor);
		object_name_label.setFont(new Font("Tahoma", Font.PLAIN, 22));
		object_editor.add(object_name_label);

		propertyTable = new JTable();
		sl_object_editor.putConstraint(SpringLayout.NORTH, propertyTable, 6, SpringLayout.SOUTH, toolBar);
		sl_object_editor.putConstraint(SpringLayout.WEST, propertyTable, 10, SpringLayout.WEST, toolBar);
		sl_object_editor.putConstraint(SpringLayout.SOUTH, propertyTable, 900, SpringLayout.SOUTH, toolBar);
		sl_object_editor.putConstraint(SpringLayout.EAST, propertyTable, -10, SpringLayout.EAST, object_editor);
		object_editor.add(propertyTable);

		save_entity_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (selectedFile != null) {
					try {
						saveEntity(currentEntityType, propertyTable, selectedFile);
					} catch (IOException | TransformerException | ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
			}
		});

		load_entity_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("res/entities/"));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					try {
						loadEntity(propertyTable, selectedFile);
						object_name_label
								.setText(selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")));
					} catch (SAXException | IOException | ParserConfigurationException e) {
						e.printStackTrace();
					}
				}
			}
		});

		create_entity_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JComboBox<String> create_entity_combobox = new JComboBox<String>(entityTypes);
				JPanel create_entity_panel = new JPanel(new FlowLayout());
				JLabel select_new_object_type_label = new JLabel("Select entyty type:");
				create_entity_panel.add(select_new_object_type_label);
				create_entity_panel.add(create_entity_combobox);
				JButton create_entity_confirm_button = new JButton("Create");
				JTextField create_entity_name = new JTextField(20);
				JFrame create_entity_frame = new JFrame("Create Entity Window");
				create_entity_frame.getContentPane().setLayout(new FlowLayout());
				create_entity_frame.getContentPane().add(create_entity_panel);
				create_entity_frame.getContentPane().add(create_entity_name);
				create_entity_frame.getContentPane().add(create_entity_confirm_button);
				create_entity_frame.setSize(250, 250);
				create_entity_frame.setVisible(true);
				create_entity_confirm_button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							selectedFile = createEntity(create_entity_combobox.getSelectedItem().toString(),
									create_entity_name.getText());
							object_name_label.setText(create_entity_name.getText());
							loadEntity(propertyTable, selectedFile);
						} catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
							e.printStackTrace();
						}
						create_entity_frame.dispose();
					}
				});
			}
		});

		generateEntitySheetList(entitySheetList);

		JPanel panel = new JPanel();
		split.setLeftComponent(panel);
		Canvas canvas = new Canvas();
		panel.add(canvas);
		canvas.setBounds(0, 0, 1280, 720);

		try {
			Display.setParent(canvas);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		frame.dispose();
	}

	private File createEntity(String entityType, String name) throws JAXBException {
		String xmlString = "";
		File file = new File("res/entities/" + name + ".xml");
		currentEntityType = entityType;
		EntitySheet entity = null;
		switch (entityType) {
		case "person":
			entity = new PersonSheet();
			break;
		case "item":
			entity = new ItemSheet();
			break;
		case "scenery":
			entity = new ScenerySheet();
			break;
		case "floor":
			entity = new FloorSheet();
			break;
		}
		entity.name = name;
		JAXBContext context = JAXBContext.newInstance(entity.getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(entity, sw);
		xmlString = sw.toString();
		stringToDom(file, xmlString);
		return file;
	}

	private void saveEntity(String entityType, JTable table, File file)
			throws IOException, TransformerException, ParserConfigurationException {
		Document dom;
		Element e = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();
		dom = db.newDocument();
		Element rootElement = dom.createElement(entityType);

		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		for (int i = 0; i <= tableModel.getRowCount() - 1; ++i) {
			String name = (String) tableModel.getValueAt(i, 0);
			String text = (String) tableModel.getValueAt(i, 1);
			e = dom.createElement(name);
			e.appendChild(dom.createTextNode(text));
			rootElement.appendChild(e);
		}

		dom.appendChild(rootElement);

		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.VERSION, "1.0");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty(OutputKeys.STANDALONE, "yes");
		tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(file)));
	}

	private void stringToDom(File file, String xmlString) {
		java.io.FileWriter fw;
		try {
			fw = new java.io.FileWriter(file);
			fw.write(xmlString);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadEntity(JTable table, File file) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		DefaultTableModel tableModel = new DefaultTableModel();
		Node root = doc.getDocumentElement();
		currentEntityType = root.getNodeName();
		NodeList list = root.getChildNodes();
		tableModel.addColumn("");
		tableModel.addColumn("");
		for (int i = 0; i < list.getLength(); ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Element.ELEMENT_NODE) {
				Object[] row = { node.getNodeName(), node.getTextContent() };
				// System.out.println(row);
				tableModel.addRow(row);
			}
		}
		table.setModel(tableModel);
	}

	private void generateEntitySheetList(List<EntitySheet> list) {
		list.clear();
		Path dir = Paths.get("res/entities");
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.{xml}")) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc;
			String rootName;
			for (Path path : paths) {
				File file = new File(path.toString());
				doc = db.parse(file);
				rootName = doc.getDocumentElement().getNodeName();
				EntitySheet entity = null;
				switch (rootName) {
				case "person":
					entity = new PersonSheet();
					break;
				case "item":
					entity = new ItemSheet();
					break;
				case "scenery":
					entity = new ScenerySheet();
					break;
				case "floor":
					entity = new FloorSheet();
					break;
				}
				JAXBContext jContext = JAXBContext.newInstance(entity.getClass());
				Unmarshaller unmarshallerObj = jContext.createUnmarshaller();
				entity = (EntitySheet) unmarshallerObj.unmarshal(file);
				list.add(entity);
			}
		} catch (IOException | SAXException | ParserConfigurationException | JAXBException e) {
			e.printStackTrace();
		}
	}

	private DefaultMutableTreeNode createObjectTreeNodes(List<EntitySheet> list) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Objects");
		for (String entityType : entityTypes) {
			DefaultMutableTreeNode entityTypeNode = new DefaultMutableTreeNode(entityType);
			root.add(entityTypeNode);
			for (EntitySheet entity : list) {
				if (entityType.equals(entity.getClass().getAnnotation(XmlRootElement.class).name())) {
					DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(entity.name);
					entityTypeNode.add(entityNode);
				}
			}
		}
		return root;
	}
}