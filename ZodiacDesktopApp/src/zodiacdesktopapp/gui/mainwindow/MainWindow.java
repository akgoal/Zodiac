package zodiacdesktopapp.gui.mainwindow;

import zodiacdesktopapp.gui.filedialogs.OpenFileDialog;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import zodiacdesktopapp.gui.filedialogs.SaveFileDialog;
import zodiacdesktopapp.gui.mainwindow.decryptingpanel.ColorDialog;
import zodiacdesktopapp.gui.mainwindow.decryptingpanel.ResNumberField;
import zodiacdesktopapp.gui.markingdialog.MarkUpWindow;
import zodiacdesktopapp.model.Model;
import zodiacdesktopapp.model.resources.ResourceHelper;
import zodiacdesktopapp.model.settings.Settings;

/**
 *
 * @author Dmitry Akishin Программа для облегчения расшифровки сообщений,
 * зашифрованных подстановкой символов. Предназначена, главным образом, для
 * разгадки смысла письма, посланного серийным убийцем, известным как Зодиак.
 * Функционал программы включает возможность разметки изоюражения с сообщением
 * (присутствует разметка по умолчанию) и средства расшифровки сообщения путем
 * подстановки символов. Кроме того, предусмотрена возможность поиска слов.
 * Область применения программы не ограничивается указанным письмом Зодиака и
 * может распространяться на другие сообщения, зашифрованные подобным образом.
 * Дата создания: 2016 год.
 */
public class MainWindow extends javax.swing.JFrame {

    /*
        Главное окно программы.
     */
    private static final String ICONS_RES_PATH = "res/icons_half_transparent.png";

    /* Модель сообщения, хранящая информацию о повторяющихся символах итд.
       Создается на основе файла разметки. */
    private Model model;

    /* Путь к используемому файлу разметки. */
    private String markUpFilePath;

    /* Текущие результаты поиска слов. */
    private ArrayList<Map<Integer, Character>> searchResults;

    /* Поиск поизводится в отдельном потоке во избежание блокировки UI. */
    private Thread searchThread;
    private Runnable searchRunnable;

    /* Начальные значения разделителей панелей. */
    private final float MAIN_SPLIT_DIVIDER_LOCATION = 0.5f;
    private final float BOTTOM_SPLIT_DIVIDER_LOCATION = 0.2f;
    private final float DECRYPTING_SPLIT_DIVIDER_LOCATION = 0.3f;
    private final float TOP_SPLIT_DIVIDER_LOCATION = 0.5f;

    /* Цвет индикатора прогресса поиска. */
    private final Color SEARCH_PROGRESS_BAR_COLOR = new Color(100, 100, 180);

    public MainWindow() {
        adjustUISettings();
        initComponents();

        Settings.getInstance().load();

        loadIcons();

        setPanels();

        loadLastSourceImageAndMarkUp();

        setSearchThread();

        adjustKeyEventDispatcher();

        pack();
    }

    /* Загрузка иконок разных разрешений. */
    private void loadIcons() {
        BufferedImage img = ResourceHelper.loadImage(ICONS_RES_PATH);
        if (img != null) {
            ArrayList<BufferedImage> icons = new ArrayList<>();
            icons.add(img.getSubimage(0, 0, 127, 127));
            icons.add(img.getSubimage(128, 0, 64, 64));
            icons.add(img.getSubimage(128, 64, 32, 32));
            icons.add(img.getSubimage(128, 96, 16, 16));
            this.setIconImages(icons);
        }
    }

    /* Загрузка последних открытых изображения и разметки. */
    private void loadLastSourceImageAndMarkUp() {
        File file = Settings.getInstance().getLastSourceImage();
        if (!(file != null && loadSourceImage(file))) {
            loadDefaultSourceImage();
        }
        file = Settings.getInstance().getLastMarkUpFile();
        if (!(file != null && loadMarkUpFile(file))) {
            loadDefaultMarkUp();
        }
    }

    /* Загрузка исходного изображения из файла. */
    private boolean loadSourceImage(File srcImg) {
        if (srcImgPanel.setImgFile(srcImg)) {
            defaultSrcImgButton.setEnabled(true);
            return true;
        }
        return false;
    }

    /* Загрузка исходного изображения по умолчанию
        (нерасшифрованное письмо Зодиака). */
    private void loadDefaultSourceImage() {
        srcImgPanel.setBufImg(ResourceHelper.loadImage(Settings.DEFAULT_SOURCE_IMAGE_PATH));
        defaultSrcImgButton.setEnabled(false);
    }

    /* Загрузка разметки изображения из файла. */
    private boolean loadMarkUpFile(File markUpFile) {
        if (createModel(markUpFile)) {
            defaultMarkUpButton.setEnabled(true);
            markUpFileLabel.setText(markUpFile.getAbsolutePath());
            markUpFilePath = markUpFile.getAbsolutePath();
            return true;
        }
        return false;
    }

    /* Загрузка разметки по умолчанию. */
    private void loadDefaultMarkUp() {
        createModel(null, ResourceHelper.loadFile(Settings.DEFAULT_MARKUP_PATH));
        defaultMarkUpButton.setEnabled(false);
        markUpFileLabel.setText("Default file");
        markUpFilePath = "Default file";
    }

    /* Поправка настроек пользовательского интерфейса.
       Заменяется стандартный для Loook&Feel цвет индикатора загрузки. */
    private void adjustUISettings() {
        UIManager.put("nimbusOrange", SEARCH_PROGRESS_BAR_COLOR);
    }

    /* Настройка панелей, внешнего вида и начальных состояний компонентов. */
    private void setPanels() {
        fontSettingsPanel.setVisible(false);
        fontColorButton.setBackground(Settings.getInstance().getFontColor());
        fontSizeSpinner.setValue(Settings.getInstance().getFontSize());

        searchPanel.setVisible(false);
        startListeningSearchResultNumberChanges();
        searchProgressBar.setMinimum(Model.getMinSearchProgress());
        searchProgressBar.setMaximum(Model.getMaxSearchProgress());
        showSearchProgressBar(false);

        mainSplitPanel.setDividerLocation(MAIN_SPLIT_DIVIDER_LOCATION);
        topSplitPanel.setDividerLocation(TOP_SPLIT_DIVIDER_LOCATION);
        bottomSplitPanel.setDividerLocation(BOTTOM_SPLIT_DIVIDER_LOCATION);
        decryptingSplitPanel.setDividerLocation(DECRYPTING_SPLIT_DIVIDER_LOCATION);

        decryptingPanel.setChildPanels(symbolsPanel, decryptedImgPanel);

        togglePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.black));
    }

    /* Показ/сброс полосы прогресса выполнения поиска. */
    private void showSearchProgressBar(boolean toShow) {
        if (!toShow) {
            searchProgressBar.setValue(Model.getMinSearchProgress());
        }
    }

    /* Поправка KeyEventDispatcher для перехвата окном событий клавиатуры. */
    private void adjustKeyEventDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {
                        if (e.getKeyCode() == Settings.getInstance().getProgramInfoKeyCode()
                                && e.getID() == KeyEvent.KEY_PRESSED
                                && e.getModifiers() == 0) {
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    Settings.getInstance().getProgramInfoText(),
                                    "Program info", JOptionPane.INFORMATION_MESSAGE);
                        }
                        return false;
                    }
                });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        togglePanel = new javax.swing.JPanel();
        sourceToggleButton = new javax.swing.JToggleButton();
        markUpToggleButton = new javax.swing.JToggleButton();
        statToggleButton = new javax.swing.JToggleButton();
        mainPanel = new javax.swing.JPanel();
        mainSplitPanel = new javax.swing.JSplitPane();
        topSplitPanel = new javax.swing.JSplitPane();
        sourcePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        srcImgPanel = new zodiacdesktopapp.gui.mainwindow.SrcImgPanel();
        openSrcImgButton = new javax.swing.JButton();
        defaultSrcImgButton = new javax.swing.JButton();
        markUpPanel = new javax.swing.JPanel();
        markedImgPanel = new zodiacdesktopapp.gui.mainwindow.MarkedImgPanel();
        openMarkUpButton = new javax.swing.JButton();
        createMarkUpButton = new javax.swing.JButton();
        markUpFileLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        defaultMarkUpButton = new javax.swing.JButton();
        bottomSplitPanel = new javax.swing.JSplitPane();
        statisticsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        statInfoPanel = new zodiacdesktopapp.gui.mainwindow.StatInfoPanel();
        decryptingPanel = new zodiacdesktopapp.gui.mainwindow.decryptingpanel.DecryptingPanel();
        decryptingSplitPanel = new javax.swing.JSplitPane();
        decryptingManagementPanel = new javax.swing.JPanel();
        fontToggleButton = new javax.swing.JToggleButton();
        fontSettingsPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        fontSizeSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        fontColorButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        symbolsPanel = new zodiacdesktopapp.gui.mainwindow.decryptingpanel.SymbolsPanel();
        bingingSettingPanel = new javax.swing.JPanel();
        saveBindingButton = new javax.swing.JButton();
        loadBindingButton = new javax.swing.JButton();
        lastBindingPanel = new javax.swing.JPanel();
        rememberBindingButton = new javax.swing.JButton();
        retrieveBindingButton = new javax.swing.JButton();
        decrImgSrchPanel = new javax.swing.JPanel();
        decryptedImgPanel = new zodiacdesktopapp.gui.mainwindow.decryptingpanel.DecryptedImgPanel();
        srchBtnPanel = new javax.swing.JPanel();
        searchPanel = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        searchProgressBar = new javax.swing.JProgressBar();
        resNavigPanel = new javax.swing.JPanel();
        prevResButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        resQntyLabel = new javax.swing.JLabel();
        nextResButton = new javax.swing.JButton();
        resNumberField = new zodiacdesktopapp.gui.mainwindow.decryptingpanel.ResNumberField();
        searchOptionsPanel = new javax.swing.JPanel();
        keepBindingCheckBox = new javax.swing.JCheckBox();
        homophonicCheckBox = new javax.swing.JCheckBox();
        directionLeftToggleButton = new javax.swing.JToggleButton();
        directionUpToggleButton = new javax.swing.JToggleButton();
        directionRightToggleButton = new javax.swing.JToggleButton();
        directionDownToggleButton = new javax.swing.JToggleButton();
        searchPnlToggleButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Zodiac");
        setLocation(new java.awt.Point(50, 50));
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(850, 400));
        setPreferredSize(new java.awt.Dimension(1100, 606));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        sourceToggleButton.setSelected(true);
        sourceToggleButton.setText("Original Image");
        sourceToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceToggleButtonActionPerformed(evt);
            }
        });

        markUpToggleButton.setSelected(true);
        markUpToggleButton.setText("Markup");
        markUpToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markUpToggleButtonActionPerformed(evt);
            }
        });

        statToggleButton.setSelected(true);
        statToggleButton.setText("Statistics");
        statToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout togglePanelLayout = new javax.swing.GroupLayout(togglePanel);
        togglePanel.setLayout(togglePanelLayout);
        togglePanelLayout.setHorizontalGroup(
            togglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(togglePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sourceToggleButton)
                .addGap(0, 0, 0)
                .addComponent(markUpToggleButton)
                .addGap(0, 0, 0)
                .addComponent(statToggleButton)
                .addContainerGap(629, Short.MAX_VALUE))
        );

        togglePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {markUpToggleButton, sourceToggleButton, statToggleButton});

        togglePanelLayout.setVerticalGroup(
            togglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(togglePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(sourceToggleButton)
                .addComponent(markUpToggleButton)
                .addComponent(statToggleButton))
        );

        mainSplitPanel.setBackground(new java.awt.Color(153, 153, 255));
        mainSplitPanel.setBorder(null);
        mainSplitPanel.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPanel.setResizeWeight(0.5);

        topSplitPanel.setBackground(new java.awt.Color(102, 102, 255));
        topSplitPanel.setBorder(null);
        topSplitPanel.setResizeWeight(0.5);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Original Image");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        srcImgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout srcImgPanelLayout = new javax.swing.GroupLayout(srcImgPanel);
        srcImgPanel.setLayout(srcImgPanelLayout);
        srcImgPanelLayout.setHorizontalGroup(
            srcImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        srcImgPanelLayout.setVerticalGroup(
            srcImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );

        openSrcImgButton.setText("Open");
        openSrcImgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSrcImgButtonActionPerformed(evt);
            }
        });

        defaultSrcImgButton.setText("Default");
        defaultSrcImgButton.setEnabled(false);
        defaultSrcImgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultSrcImgButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sourcePanelLayout = new javax.swing.GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(srcImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sourcePanelLayout.createSequentialGroup()
                        .addGap(0, 166, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(0, 166, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(defaultSrcImgButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openSrcImgButton)))
                .addContainerGap())
        );
        sourcePanelLayout.setVerticalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(srcImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openSrcImgButton)
                    .addComponent(defaultSrcImgButton))
                .addContainerGap())
        );

        topSplitPanel.setLeftComponent(sourcePanel);

        markedImgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout markedImgPanelLayout = new javax.swing.GroupLayout(markedImgPanel);
        markedImgPanel.setLayout(markedImgPanelLayout);
        markedImgPanelLayout.setHorizontalGroup(
            markedImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        markedImgPanelLayout.setVerticalGroup(
            markedImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );

        openMarkUpButton.setText("Open");
        openMarkUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMarkUpButtonActionPerformed(evt);
            }
        });

        createMarkUpButton.setText("Create");
        createMarkUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMarkUpButtonActionPerformed(evt);
            }
        });

        markUpFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        markUpFileLabel.setText("The file is not loaded");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Markup");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        defaultMarkUpButton.setText("Default");
        defaultMarkUpButton.setEnabled(false);
        defaultMarkUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultMarkUpButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout markUpPanelLayout = new javax.swing.GroupLayout(markUpPanel);
        markUpPanel.setLayout(markUpPanelLayout);
        markUpPanelLayout.setHorizontalGroup(
            markUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(markUpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(markUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(markUpPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(markUpPanelLayout.createSequentialGroup()
                        .addGroup(markUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(markedImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(markUpPanelLayout.createSequentialGroup()
                                .addComponent(markUpFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(defaultMarkUpButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(createMarkUpButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openMarkUpButton)))
                        .addContainerGap())))
        );
        markUpPanelLayout.setVerticalGroup(
            markUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(markUpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(markedImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(markUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openMarkUpButton)
                    .addComponent(createMarkUpButton)
                    .addComponent(markUpFileLabel)
                    .addComponent(defaultMarkUpButton))
                .addContainerGap())
        );

        topSplitPanel.setRightComponent(markUpPanel);

        mainSplitPanel.setLeftComponent(topSplitPanel);

        bottomSplitPanel.setBackground(new java.awt.Color(153, 153, 255));
        bottomSplitPanel.setBorder(null);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Statistics");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        statInfoPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout statInfoPanelLayout = new javax.swing.GroupLayout(statInfoPanel);
        statInfoPanel.setLayout(statInfoPanelLayout);
        statInfoPanelLayout.setHorizontalGroup(
            statInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );
        statInfoPanelLayout.setVerticalGroup(
            statInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(statInfoPanel);

        javax.swing.GroupLayout statisticsPanelLayout = new javax.swing.GroupLayout(statisticsPanel);
        statisticsPanel.setLayout(statisticsPanelLayout);
        statisticsPanelLayout.setHorizontalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        statisticsPanelLayout.setVerticalGroup(
            statisticsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statisticsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
        );

        bottomSplitPanel.setLeftComponent(statisticsPanel);

        decryptingPanel.setBackground(new java.awt.Color(153, 153, 255));

        decryptingSplitPanel.setBorder(null);

        fontToggleButton.setText("Font");
        fontToggleButton.setEnabled(false);
        fontToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontToggleButtonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Size");

        fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(22, 6, 99, 1));
        fontSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fontSizeSpinnerStateChanged(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("Color");

        fontColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fontSettingsPanelLayout = new javax.swing.GroupLayout(fontSettingsPanel);
        fontSettingsPanel.setLayout(fontSettingsPanelLayout);
        fontSettingsPanelLayout.setHorizontalGroup(
            fontSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontSettingsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fontSettingsPanelLayout.setVerticalGroup(
            fontSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontSettingsPanelLayout.createSequentialGroup()
                .addGroup(fontSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(fontColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        symbolsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        symbolsPanel.setToolTipText("");

        javax.swing.GroupLayout symbolsPanelLayout = new javax.swing.GroupLayout(symbolsPanel);
        symbolsPanel.setLayout(symbolsPanelLayout);
        symbolsPanelLayout.setHorizontalGroup(
            symbolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 366, Short.MAX_VALUE)
        );
        symbolsPanelLayout.setVerticalGroup(
            symbolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(symbolsPanel);

        saveBindingButton.setText("Save");
        saveBindingButton.setEnabled(false);
        saveBindingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBindingButtonActionPerformed(evt);
            }
        });

        loadBindingButton.setText("Load");
        loadBindingButton.setEnabled(false);
        loadBindingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadBindingButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bingingSettingPanelLayout = new javax.swing.GroupLayout(bingingSettingPanel);
        bingingSettingPanel.setLayout(bingingSettingPanelLayout);
        bingingSettingPanelLayout.setHorizontalGroup(
            bingingSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bingingSettingPanelLayout.createSequentialGroup()
                .addComponent(saveBindingButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(loadBindingButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bingingSettingPanelLayout.setVerticalGroup(
            bingingSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bingingSettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(saveBindingButton)
                .addComponent(loadBindingButton))
        );

        rememberBindingButton.setText("Remember");
        rememberBindingButton.setEnabled(false);
        rememberBindingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rememberBindingButtonActionPerformed(evt);
            }
        });

        retrieveBindingButton.setText("Retrieve");
        retrieveBindingButton.setEnabled(false);
        retrieveBindingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveBindingButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lastBindingPanelLayout = new javax.swing.GroupLayout(lastBindingPanel);
        lastBindingPanel.setLayout(lastBindingPanelLayout);
        lastBindingPanelLayout.setHorizontalGroup(
            lastBindingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lastBindingPanelLayout.createSequentialGroup()
                .addComponent(rememberBindingButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(retrieveBindingButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        lastBindingPanelLayout.setVerticalGroup(
            lastBindingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lastBindingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(rememberBindingButton)
                .addComponent(retrieveBindingButton))
        );

        javax.swing.GroupLayout decryptingManagementPanelLayout = new javax.swing.GroupLayout(decryptingManagementPanel);
        decryptingManagementPanel.setLayout(decryptingManagementPanelLayout);
        decryptingManagementPanelLayout.setHorizontalGroup(
            decryptingManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fontToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(bingingSettingPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(fontSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lastBindingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        decryptingManagementPanelLayout.setVerticalGroup(
            decryptingManagementPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decryptingManagementPanelLayout.createSequentialGroup()
                .addComponent(fontToggleButton)
                .addGap(0, 0, 0)
                .addComponent(fontSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lastBindingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(bingingSettingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        decryptingSplitPanel.setLeftComponent(decryptingManagementPanel);

        javax.swing.GroupLayout decryptedImgPanelLayout = new javax.swing.GroupLayout(decryptedImgPanel);
        decryptedImgPanel.setLayout(decryptedImgPanelLayout);
        decryptedImgPanelLayout.setHorizontalGroup(
            decryptedImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        decryptedImgPanelLayout.setVerticalGroup(
            decryptedImgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 223, Short.MAX_VALUE)
        );

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        searchField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchFieldKeyPressed(evt);
            }
        });

        searchProgressBar.setFont(new java.awt.Font("Tahoma", 0, 5)); // NOI18N
        searchProgressBar.setForeground(new java.awt.Color(123, 122, 226));
        searchProgressBar.setMinimumSize(new java.awt.Dimension(10, 10));
        searchProgressBar.setPreferredSize(new java.awt.Dimension(146, 10));
        searchProgressBar.setString("");

        prevResButton.setText("<");
        prevResButton.setEnabled(false);
        prevResButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevResButtonActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText(" /");

        resQntyLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        resQntyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        resQntyLabel.setText("0");

        nextResButton.setText(">");
        nextResButton.setEnabled(false);
        nextResButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextResButtonActionPerformed(evt);
            }
        });

        resNumberField.setText("0");

        javax.swing.GroupLayout resNavigPanelLayout = new javax.swing.GroupLayout(resNavigPanel);
        resNavigPanel.setLayout(resNavigPanelLayout);
        resNavigPanelLayout.setHorizontalGroup(
            resNavigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resNavigPanelLayout.createSequentialGroup()
                .addComponent(prevResButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resQntyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextResButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        resNavigPanelLayout.setVerticalGroup(
            resNavigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resNavigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(nextResButton)
                .addComponent(prevResButton)
                .addComponent(resNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel7)
                .addComponent(resQntyLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        resNavigPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel7, resNumberField});

        keepBindingCheckBox.setText("Keep binding");

        homophonicCheckBox.setText("Homophonic");

        directionLeftToggleButton.setText("←");
        directionLeftToggleButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        directionLeftToggleButton.setBorderPainted(false);
        directionLeftToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directionLeftToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        directionUpToggleButton.setText("↑");
        directionUpToggleButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        directionUpToggleButton.setBorderPainted(false);
        directionUpToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directionUpToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        directionUpToggleButton.setMinimumSize(null);
        directionUpToggleButton.setPreferredSize(null);

        directionRightToggleButton.setSelected(true);
        directionRightToggleButton.setText("→");
        directionRightToggleButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        directionRightToggleButton.setBorderPainted(false);
        directionRightToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directionRightToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        directionRightToggleButton.setMinimumSize(null);
        directionRightToggleButton.setPreferredSize(null);

        directionDownToggleButton.setText("↓");
        directionDownToggleButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        directionDownToggleButton.setBorderPainted(false);
        directionDownToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directionDownToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        directionDownToggleButton.setMinimumSize(null);
        directionDownToggleButton.setPreferredSize(null);

        javax.swing.GroupLayout searchOptionsPanelLayout = new javax.swing.GroupLayout(searchOptionsPanel);
        searchOptionsPanel.setLayout(searchOptionsPanelLayout);
        searchOptionsPanelLayout.setHorizontalGroup(
            searchOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchOptionsPanelLayout.createSequentialGroup()
                .addComponent(keepBindingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(homophonicCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(directionLeftToggleButton)
                .addGap(0, 0, 0)
                .addComponent(directionUpToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(directionRightToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(directionDownToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        searchOptionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {directionDownToggleButton, directionLeftToggleButton, directionRightToggleButton, directionUpToggleButton});

        searchOptionsPanelLayout.setVerticalGroup(
            searchOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(keepBindingCheckBox)
                .addComponent(homophonicCheckBox)
                .addComponent(directionLeftToggleButton)
                .addComponent(directionUpToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(directionRightToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(directionDownToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton))
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(searchOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 261, Short.MAX_VALUE)
                        .addComponent(resNavigPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchButton)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(searchProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(searchOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resNavigPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        searchPnlToggleButton.setText("Search");
        searchPnlToggleButton.setEnabled(false);
        searchPnlToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPnlToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout srchBtnPanelLayout = new javax.swing.GroupLayout(srchBtnPanel);
        srchBtnPanel.setLayout(srchBtnPanelLayout);
        srchBtnPanelLayout.setHorizontalGroup(
            srchBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchPnlToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        srchBtnPanelLayout.setVerticalGroup(
            srchBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(srchBtnPanelLayout.createSequentialGroup()
                .addComponent(searchPnlToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout decrImgSrchPanelLayout = new javax.swing.GroupLayout(decrImgSrchPanel);
        decrImgSrchPanel.setLayout(decrImgSrchPanelLayout);
        decrImgSrchPanelLayout.setHorizontalGroup(
            decrImgSrchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(srchBtnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(decryptedImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        decrImgSrchPanelLayout.setVerticalGroup(
            decrImgSrchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(decrImgSrchPanelLayout.createSequentialGroup()
                .addComponent(srchBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decryptedImgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        decryptingSplitPanel.setRightComponent(decrImgSrchPanel);

        javax.swing.GroupLayout decryptingPanelLayout = new javax.swing.GroupLayout(decryptingPanel);
        decryptingPanel.setLayout(decryptingPanelLayout);
        decryptingPanelLayout.setHorizontalGroup(
            decryptingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(decryptingSplitPanel)
        );
        decryptingPanelLayout.setVerticalGroup(
            decryptingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(decryptingSplitPanel)
        );

        bottomSplitPanel.setRightComponent(decryptingPanel);

        mainSplitPanel.setRightComponent(bottomSplitPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(mainSplitPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(mainSplitPanel))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(togglePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(togglePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /* Загрузка файла разметки. */
    private void openMarkUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMarkUpButtonActionPerformed
        OpenFileDialog openDialog = new OpenFileDialog("Open markup file");
        openDialog.setDirectory(Settings.getInstance().getLastDirectoryForMarkUpFile());
        openDialog.setFileFilter("Zodiac markup files", Settings.getInstance().getMarkUpFileExtension());
        int retrival = openDialog.showOpenDialog(this);
        if (retrival == OpenFileDialog.APPROVE_OPTION) {
            File file = openDialog.getSelectedFile();
            if (loadMarkUpFile(file)) {
                Settings.getInstance().setLastMarkUpFile(file);
            }
            Settings.getInstance().setLastDirectoryForMarkUpFile(openDialog.getCurrentDirectory());
        }
    }//GEN-LAST:event_openMarkUpButtonActionPerformed

    /* Открытие окна создания разметки. */
    private void createMarkUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMarkUpButtonActionPerformed
        MarkUpWindow markUpWindow = new MarkUpWindow(this, srcImgPanel.getImg(),
                new MarkUpWindow.OnMarkedListener() {
            @Override
            public void setMarkedFile(String markedFile) {
                createModel(markedFile, null);
            }
        });
    }//GEN-LAST:event_createMarkUpButtonActionPerformed

    /* Показ/скрытие панели с исходным изображением. */
    private void sourceToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceToggleButtonActionPerformed
        sourcePanel.setVisible(!sourcePanel.isVisible());
        adjustTopPanel();
    }//GEN-LAST:event_sourceToggleButtonActionPerformed

    /* Показ/скрытие панели с разметкой. */
    private void markUpToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markUpToggleButtonActionPerformed
        markUpPanel.setVisible(!markUpPanel.isVisible());
        adjustTopPanel();
    }//GEN-LAST:event_markUpToggleButtonActionPerformed

    /* Позиции разделителей панелей */
    private float topPanelLastDividerProportion = TOP_SPLIT_DIVIDER_LOCATION,
            mainPanelLastDividerProportion = MAIN_SPLIT_DIVIDER_LOCATION,
            bottomPanelLastDividerProportion = BOTTOM_SPLIT_DIVIDER_LOCATION;

    /* "Поправка" разделителя верхней панели 
        (панели исходного изображения и разметки). 
        Необходима для корретной обработки скрытия/показа панелей, в т.ч. запоминания
        позиции разделителя во время скрытия одной из дочерних панелей и последующего 
        возврата старого значения при показе этой панели. */
    private void adjustTopPanel() {
        boolean mainSplitPanelWasJustRestored = false;
        if (sourcePanel.isVisible()) {
            if (!mainSplitPanel.isEnabled()) {
                mainSplitPanel.setEnabled(true);
                mainSplitPanel.setDividerLocation(mainPanelLastDividerProportion);
                mainSplitPanelWasJustRestored = true;
            } else {
                mainSplitPanelWasJustRestored = false;
            }
            if (markUpPanel.isVisible()) {
                topSplitPanel.setEnabled(true);
                topSplitPanel.setDividerLocation(topPanelLastDividerProportion);
            } else {
                if (!mainSplitPanelWasJustRestored) {
                    topPanelLastDividerProportion = topSplitPanel.getDividerLocation() / (float) topSplitPanel.getSize().width;
                }
                topSplitPanel.setDividerLocation(mainSplitPanel.getSize().width);
                topSplitPanel.setEnabled(false);
            }
        } else if (markUpPanel.isVisible()) {
            if (!mainSplitPanel.isEnabled()) {
                mainSplitPanel.setEnabled(true);
                mainSplitPanel.setDividerLocation(mainPanelLastDividerProportion);
            } else {
                topPanelLastDividerProportion = topSplitPanel.getDividerLocation() / (float) topSplitPanel.getSize().width;
            }
            topSplitPanel.setDividerLocation(0);
            topSplitPanel.setEnabled(false);
        } else {
            mainPanelLastDividerProportion
                    = mainSplitPanel.getDividerLocation() / (float) mainSplitPanel.getSize().height;
            mainSplitPanel.setDividerLocation(0);
            mainSplitPanel.setEnabled(false);
        }
    }

    /* Скрытие/показ панели статистики. */
    private void statToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statToggleButtonActionPerformed
        statisticsPanel.setVisible(!statisticsPanel.isVisible());
        adjustBottomPanel();
    }//GEN-LAST:event_statToggleButtonActionPerformed

    /* Попрака нижней панели (статистика и главная панель). */
    private void adjustBottomPanel() {
        if (statisticsPanel.isVisible()) {
            bottomSplitPanel.setEnabled(true);
            bottomSplitPanel.setDividerLocation(bottomPanelLastDividerProportion);
        } else {
            bottomPanelLastDividerProportion
                    = bottomSplitPanel.getDividerLocation() / (float) bottomSplitPanel.getSize().width;
            bottomSplitPanel.setEnabled(false);
            bottomSplitPanel.setDividerLocation(0);
        }
    }

    /* Изменение цвета шрифта "вписываемых" символов. */
    private void fontColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontColorButtonActionPerformed
        ColorDialog colorDialog = new ColorDialog(this, true, "Color font",
                Settings.getInstance().getFontColor(), new ColorDialog.OnColorChosenListener() {
            @Override
            public void action(Color color) {
                Settings.getInstance().setFontColor(color);
                fontColorButton.setBackground(Settings.getInstance().getFontColor());
                decryptedImgPanel.repaint();
            }
        });
        colorDialog.setVisible(true);
    }//GEN-LAST:event_fontColorButtonActionPerformed

    /* Изменение размера шрифта. */
    private void fontSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fontSizeSpinnerStateChanged
        Settings.getInstance().setFontSize((int) fontSizeSpinner.getValue());
        decryptedImgPanel.repaint();
    }//GEN-LAST:event_fontSizeSpinnerStateChanged

    /* Показ/скрытие панели с настройками шрифта */
    private void fontToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontToggleButtonActionPerformed
        fontSettingsPanel.setVisible(!fontSettingsPanel.isVisible());
    }//GEN-LAST:event_fontToggleButtonActionPerformed

    /* Обработка изменения размеров окна для корректного поведения разделителя между
        верхней и нижнней панелями. */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        if (!mainSplitPanel.isEnabled()) {
            mainSplitPanel.setDividerLocation(0);
        }
    }//GEN-LAST:event_formComponentResized

    /* Сохранение привязки символов. */
    private void saveBindingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBindingButtonActionPerformed
        SaveFileDialog saveDialog = new SaveFileDialog("Save binding");
        saveDialog.setFileFilter("Zodiac binding files", Settings.getInstance().getBindingFileExtension());
        saveDialog.setCurrentDirectory(Settings.getInstance().getLastDirectoryForBindingFile());
        int retrival = saveDialog.showSaveDialog(this);
        if (retrival == SaveFileDialog.APPROVE_OPTION) {
            symbolsPanel.saveToFile(saveDialog.getSelectedFile(), markUpFilePath);
            Settings.getInstance().setLastDirectoryForBindingFile(saveDialog.getCurrentDirectory());
        }
    }//GEN-LAST:event_saveBindingButtonActionPerformed

    /* Загрузка привязки символов. */
    private void loadBindingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadBindingButtonActionPerformed
        OpenFileDialog openDialog = new OpenFileDialog("Load binding");
        openDialog.setDirectory(Settings.getInstance().getLastDirectoryForBindingFile());
        openDialog.setFileFilter("Zodiac binding files", Settings.getInstance().getBindingFileExtension());
        int retrival = openDialog.showOpenDialog(this);
        if (retrival == OpenFileDialog.APPROVE_OPTION) {
            clearSearchInfo();
            symbolsPanel.loadFromFile(openDialog.getSelectedFile(), markUpFilePath, this);
            Settings.getInstance().setLastDirectoryForBindingFile(openDialog.getCurrentDirectory());
        }
    }//GEN-LAST:event_loadBindingButtonActionPerformed

    /* Сохранение настроек при закрытии приложения. */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        Settings.getInstance().save();
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    /* Настройка потока поиска слов, а точнее объекта Runnable для работы потока. */
    private void setSearchThread() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                String[] words = searchField.getText().split(" ");
                if (searchField.getText().equals("") || words.length == 0) {
                    return;
                }

                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        prevResButton.setEnabled(false);
                        nextResButton.setEnabled(false);
                        showSearchProgressBar(true);
                    }
                });

                searchResults = model.searchWords(words,
                        keepBindingCheckBox.isSelected() ? symbolsPanel.getBinding()
                                : null,
                        homophonicCheckBox.isSelected(),
                        directionLeftToggleButton.isSelected(),
                        directionUpToggleButton.isSelected(),
                        directionRightToggleButton.isSelected(),
                        directionDownToggleButton.isSelected(),
                        new Model.SearchProgressListener() {
                    @Override
                    public void onProgressChanged(int progressValue) {
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                searchProgressBar.setValue(progressValue);
                            }
                        });
                    }
                });

                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        resQntyLabel.setText("" + searchResults.size());
                        resNumberField.setMaxNumber(searchResults.size());
                        prevResButton.setEnabled(!resNumberField.isOnMin());
                        nextResButton.setEnabled(!resNumberField.isOnMax());
                        showSearchProgressBar(false);
                    }
                });
            }
        };
    }

    /* Инициация поиска. */
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        if (model != null) {
            stopSearchThread();
            searchThread = new Thread(searchRunnable);
            searchThread.start();
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    /* Остановка потока поиска. */
    private void stopSearchThread() {
        if (searchThread != null && searchThread.isAlive()) {
            searchThread.stop();
        }
    }

    /* Организация прослушивания навигации по результатам поиска для 
        обновления привязки символов. */
    private void startListeningSearchResultNumberChanges() {
        resNumberField.setOnNumberChangedListener(new ResNumberField.OnNumberChangedListener() {
            @Override
            public void onNumberChangedAction(int number, boolean isOnMin, boolean isOnMax) {
                if (searchResults != null) {
                    if ((number >= 1) && (number <= searchResults.size())) {
                        symbolsPanel.setBinding(searchResults.get(number - 1));
                    }
                }
                prevResButton.setEnabled(!isOnMin);
                nextResButton.setEnabled(!isOnMax);
            }
        });
    }

    /* Скрытие/показ панели поиска. */
    private void searchPnlToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPnlToggleButtonActionPerformed
        searchPanel.setVisible(!searchPanel.isVisible());
    }//GEN-LAST:event_searchPnlToggleButtonActionPerformed

    /* Переход на предыдущий результат поиска. */
    private void prevResButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevResButtonActionPerformed
        resNumberField.decreaseNumber();
    }//GEN-LAST:event_prevResButtonActionPerformed

    /* Переход на следующий результат поиска. */
    private void nextResButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextResButtonActionPerformed
        resNumberField.increaseNumber();
    }//GEN-LAST:event_nextResButtonActionPerformed

    /* Организация возможности начала поиска при нажатии клавиши Enter. */
    private void searchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            searchButtonActionPerformed(null);
        }
    }//GEN-LAST:event_searchFieldKeyPressed

    /* Запоминание текущей привязки символов. */
    private void rememberBindingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rememberBindingButtonActionPerformed
        symbolsPanel.rememberBinding();
        retrieveBindingButton.setEnabled(true);
    }//GEN-LAST:event_rememberBindingButtonActionPerformed

    /* Возврат к запомненной привязке */
    private void retrieveBindingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveBindingButtonActionPerformed
        symbolsPanel.retrieveBinding();
    }//GEN-LAST:event_retrieveBindingButtonActionPerformed

    /* Переход к исходному изображению по умолчанию. */
    private void defaultSrcImgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultSrcImgButtonActionPerformed
        int res = JOptionPane.showConfirmDialog(this,
                "All unsaved changes will be lost. Continue anyway?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        switch (res) {
            case JOptionPane.YES_OPTION:
                break;
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CLOSED_OPTION:
                return;
            default:
                return;
        }
        loadDefaultSourceImage();
        Settings.getInstance().setLastSourceImage(null);
        clearMarkUp();
    }//GEN-LAST:event_defaultSrcImgButtonActionPerformed

    /* Открытие исходного изображения. */
    private void openSrcImgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSrcImgButtonActionPerformed
        OpenFileDialog openDialog = new OpenFileDialog("Open source file");
        openDialog.setDirectory(Settings.getInstance().getLastDirectoryForSourceImage());
        openDialog.enableAllFilesFilter();
        int retrival = openDialog.showOpenDialog(this);
        if (retrival == OpenFileDialog.APPROVE_OPTION) {
            Settings.getInstance().
                    setLastDirectoryForSourceImage(openDialog.getCurrentDirectory());
            File file = openDialog.getSelectedFile();
            if (loadSourceImage(file)) {
                Settings.getInstance().setLastSourceImage(file);
                clearMarkUp();
            }
        }
    }//GEN-LAST:event_openSrcImgButtonActionPerformed

    /* Переход к разметке по умолчанию. */
    private void defaultMarkUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultMarkUpButtonActionPerformed
        int res = JOptionPane.showConfirmDialog(this,
                "All unsaved changes will be lost. Continue anyway?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        switch (res) {
            case JOptionPane.YES_OPTION:
                break;
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CLOSED_OPTION:
                return;
            default:
                return;
        }
        loadDefaultMarkUp();
        Settings.getInstance().setLastMarkUpFile(null);
    }//GEN-LAST:event_defaultMarkUpButtonActionPerformed

    /* Стирание всей информации о разметке. */
    private void clearMarkUp() {
        this.markUpFilePath = null;
        this.model = null;
        markUpFileLabel.setText("The file is not loaded");
        markedImgPanel.update(model);
        statInfoPanel.update(model);

        decryptingPanel.setModel(model);
        decryptingPanel.update();

        fontToggleButton.setEnabled(false);
        fontSettingsPanel.setVisible(false);

        searchPnlToggleButton.setSelected(false);
        searchPanel.setVisible(false);
        rememberBindingButton.setEnabled(false);
        retrieveBindingButton.setEnabled(false);
        symbolsPanel.clearRememberedBinding();
        saveBindingButton.setEnabled(false);
        loadBindingButton.setEnabled(false);

        clearSearchInfo();
    }

    /* Создание модели зашифрованного сообщения из файла разметки. */
    private boolean createModel(File markUpFile) {
        if (!markUpFile.exists()) {
            return false;
        }
        return createModel(markUpFile.getAbsolutePath(), null);
    }

    /* Создание модели зашифрованного сообщения либо из файла разметки по пути к нему, 
        либо по объекту класса InputStream. Выбор источника разметки осуществляется 
        через передачу в качестве другого параметра значения null. 
        При ненулевом значении обоих параметров предпочтение отдается первому. */
    private boolean createModel(String markUpFile, InputStream inputStream) {
        if (markUpFile == null && inputStream == null) {
            return false;
        }

        Model prevModel = null;
        if (model != null) {
            prevModel = model.cloneModel();
        }
        model = new Model(srcImgPanel.getImg().getImg());
        boolean modelCreated;
        if (markUpFile != null) {
            modelCreated = model.setModel(markUpFile);
        } else {
            modelCreated = model.setModel(inputStream);
        }
        if (modelCreated) {
            markedImgPanel.update(model);
            statInfoPanel.update(model);

            decryptingPanel.setModel(model);
            decryptingPanel.update();

            fontToggleButton.setEnabled(true);
            rememberBindingButton.setEnabled(true);
            retrieveBindingButton.setEnabled(false);
            symbolsPanel.clearRememberedBinding();
            saveBindingButton.setEnabled(true);
            loadBindingButton.setEnabled(true);

            clearSearchInfo();

            searchPnlToggleButton.setEnabled(true);

            return true;
        } else {
            model = prevModel;
            JOptionPane.showMessageDialog(this,
                    "Unable to create model: the markup file is invalid.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /* Стирание информации о поиске. */
    private void clearSearchInfo() {
        if (searchResults != null) {
            searchResults.clear();
        }
        searchField.setText("");
        resQntyLabel.setText("0");
        resNumberField.setMaxNumber(0);
        prevResButton.setEnabled(!resNumberField.isOnMin());
        nextResButton.setEnabled(!resNumberField.isOnMax());
        stopSearchThread();
        showSearchProgressBar(false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bingingSettingPanel;
    private javax.swing.JSplitPane bottomSplitPanel;
    private javax.swing.JButton createMarkUpButton;
    private javax.swing.JPanel decrImgSrchPanel;
    private zodiacdesktopapp.gui.mainwindow.decryptingpanel.DecryptedImgPanel decryptedImgPanel;
    private javax.swing.JPanel decryptingManagementPanel;
    private zodiacdesktopapp.gui.mainwindow.decryptingpanel.DecryptingPanel decryptingPanel;
    private javax.swing.JSplitPane decryptingSplitPanel;
    private javax.swing.JButton defaultMarkUpButton;
    private javax.swing.JButton defaultSrcImgButton;
    private javax.swing.JToggleButton directionDownToggleButton;
    private javax.swing.JToggleButton directionLeftToggleButton;
    private javax.swing.JToggleButton directionRightToggleButton;
    private javax.swing.JToggleButton directionUpToggleButton;
    private javax.swing.JButton fontColorButton;
    private javax.swing.JPanel fontSettingsPanel;
    private javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JToggleButton fontToggleButton;
    private javax.swing.JCheckBox homophonicCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox keepBindingCheckBox;
    private javax.swing.JPanel lastBindingPanel;
    private javax.swing.JButton loadBindingButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPanel;
    private javax.swing.JLabel markUpFileLabel;
    private javax.swing.JPanel markUpPanel;
    private javax.swing.JToggleButton markUpToggleButton;
    private zodiacdesktopapp.gui.mainwindow.MarkedImgPanel markedImgPanel;
    private javax.swing.JButton nextResButton;
    private javax.swing.JButton openMarkUpButton;
    private javax.swing.JButton openSrcImgButton;
    private javax.swing.JButton prevResButton;
    private javax.swing.JButton rememberBindingButton;
    private javax.swing.JPanel resNavigPanel;
    private zodiacdesktopapp.gui.mainwindow.decryptingpanel.ResNumberField resNumberField;
    private javax.swing.JLabel resQntyLabel;
    private javax.swing.JButton retrieveBindingButton;
    private javax.swing.JButton saveBindingButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JPanel searchOptionsPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JToggleButton searchPnlToggleButton;
    private javax.swing.JProgressBar searchProgressBar;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JToggleButton sourceToggleButton;
    private zodiacdesktopapp.gui.mainwindow.SrcImgPanel srcImgPanel;
    private javax.swing.JPanel srchBtnPanel;
    private zodiacdesktopapp.gui.mainwindow.StatInfoPanel statInfoPanel;
    private javax.swing.JToggleButton statToggleButton;
    private javax.swing.JPanel statisticsPanel;
    private zodiacdesktopapp.gui.mainwindow.decryptingpanel.SymbolsPanel symbolsPanel;
    private javax.swing.JPanel togglePanel;
    private javax.swing.JSplitPane topSplitPanel;
    // End of variables declaration//GEN-END:variables
}
