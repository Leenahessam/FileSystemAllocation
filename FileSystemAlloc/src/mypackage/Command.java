package mypackage;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Command {
    public static Vector<String> Blocks = new Vector<>();
    public static Vector<String> Directories = new Vector<>();
    public static Vector<ContiguousAlloc> conAlloc = new Vector<>();
    public static Vector<IndexedAlloc> IndAlloc = new Vector<>();
    public static int num;
    public static String fileName = "DiskStructure.vfs";

    //taking data from file
    public Command() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Which allocation method will you use ?\n1- Contiguous allocation  2- Indexed allocation");
        num = scan.nextInt();
        try {
            File f = new File(fileName);
            Scanner myReader = new Scanner(f);
            int flag = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.equals("*")) {
                    flag++;
                    continue;
                }
                //adding blocks
                if (flag == 0) {
                    for (int i = 0; i < data.length(); i++)
                        Blocks.add(data.charAt(i) + "");
                }
                //adding the Files allocating blocks
                int x = 0;
                int len = 0;
                if (flag == 1) {
                    Vector<String> FileAlloc = new Vector<>();
                    for (int i = 0; i < data.length(); i++) {
                        if (data.charAt(i) == ' ' || data.charAt(i) == '/') {
                            String temp = data.substring(x, x + len);
                            x += len + 1;
                            len = 0;
                            FileAlloc.add(temp);
                            temp = "";
                            continue;
                        }
                        len++;
                    }

                    //contiguous allocation
                    if (num == 1) {
                        ContiguousAlloc temp = new ContiguousAlloc();
                        temp.path = "";
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ' ')
                                break;
                            temp.path += data.charAt(i) + "";
                        }
                        for (int j = 0; j < FileAlloc.size() - 1; j++) {
                            if (FileAlloc.get(j).contains(".")) {
                                temp.dir.add(FileAlloc.get(j));
                                temp.start = parseInt(FileAlloc.get(j + 1));
                                temp.length = parseInt(FileAlloc.get(j + 2));
                                conAlloc.add(temp);
                                break;
                            }
                            temp.dir.add(FileAlloc.get(j));
                        }
                    } else if (num == 2) {
                        //String Data = data.replaceAll("\\s", "");
                        IndexedAlloc temp = new IndexedAlloc();
                        temp.path = "";
                        for (int j = 0; j < FileAlloc.size() - 1; j++) {
                            if (FileAlloc.get(j).contains(".")) {
                                temp.dir.add(FileAlloc.get(j));
                                temp.index = parseInt(FileAlloc.get(j + 1));
                                j += 2;
                                while (!FileAlloc.get(j).equals(",")) {
                                    temp.blocks.add(parseInt(FileAlloc.get(j)));
                                    j++;
                                }
                                break;
                            }
                            temp.dir.add(FileAlloc.get(j));
                        }
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ' ')
                                break;
                            temp.path += data.charAt(i) + "";
                        }
                        IndAlloc.add(temp);
                    }
                }
                if (flag == 2)
                    Directories.add(data);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int bestFit(int len) {
        int min = Integer.MAX_VALUE;
        int counter = 0;
        int start;
        int index = -1;
        for (int i = 0; i < Blocks.size(); i++) {
            if (Blocks.get(i).equals("0")) {
                start = i;
                i++;
                counter = 1;
                while (!Blocks.get(i).equals("1")) {
                    counter++;
                    if (i == Blocks.size() - 1)
                        break;
                    i++;
                }
                if (counter < len)
                    continue;
                else if (counter == len) {
                    index = start;
                    min = counter;
                } else if (counter < min) {
                    min = counter;
                    index = start;
                }
            }
        }
        return index;
    }

    public void CreateFile(String[] command, String com) {
        command[1] += "/";
        String dir[] = command[1].split("/", com.length() - 1);
        String direct = "";
        int len = parseInt(command[command.length - 2]);
        for (int i = 0; i < dir.length - 2; i++) {
            direct += dir[i];
            if (i != dir.length - 3)
                direct += "/";
        }
        if (Directories.contains(direct) && !Directories.contains(command[1].substring(0, command[1].length() - 1))) {
            //contiguous allocation
            if (num == 1) {
                int start = bestFit(len);
                if (start == -1)
                    System.out.println("No enough space");
                else {
                    for (int i = start; i < start + len; i++)
                        Blocks.set(i, "1");
                    ContiguousAlloc temp = new ContiguousAlloc();
                    temp.path = command[1].substring(0, command[1].length() - 1);
                    for (int i = 0; i < dir.length; i++)
                        temp.dir.add(dir[i]);
                    temp.start = start;
                    temp.length = len;
                    conAlloc.add(temp);
                    Directories.add(temp.path);
                }
            }
            //indexed allocation
            else if (num == 2) {
                int added = 0;
                Vector<Integer> index = new Vector();
                for (int i = 0; i < Blocks.size(); i++) {
                    if (added == len + 1)
                        break;
                    if (Blocks.get(i).equals("0"))   //index
                    {
                        index.add(i);
                        added++;
                    }
                }
                if (added == len + 1) {
                    IndexedAlloc temp = new IndexedAlloc();
                    temp.index = index.get(0);
                    temp.path = command[1].substring(0, command[1].length() - 1);
                    for (int i = 0; i < dir.length; i++)
                        temp.dir.add(dir[i]);
                    for (int i = 1; i < index.size(); i++)
                        temp.blocks.add(index.get(i));
                    IndAlloc.add(temp);
                    Directories.add(temp.path);
                    for (int i = 0; i < index.size(); i++)
                        Blocks.set(index.get(i), "1");

                } else
                    System.out.println("No enough space");
            }
        } else
            System.out.println("can't create such file ");

    }

    public void CreateFolder(String[] command, String com) {
        command[1] += "/";
        String dir[] = command[1].split("/", com.length() - 1);
        String direct = "";
        for (int i = 0; i < dir.length - 2; i++) {
            direct += dir[i];
            if (i != dir.length - 3)
                direct += "/";
        }
        if (Directories.contains(direct) && !Directories.contains(command[1].substring(0, command[1].length() - 1)))
            Directories.add(command[1].substring(0, command[1].length() - 1));
        else
            System.out.println("Can't create folder with such name ");
    }

    public void DeleteFile(String path) {
        Boolean check = false;
        for (int i = 0; i < Directories.size(); i++) {
            if (Directories.get(i).equals(path)) {
                check = true;
                Directories.remove(i);
                if (num == 1) {
                    int start = -1;
                    int length = 0;
                    for (int j = 0; j < conAlloc.size(); j++) {
                        if (conAlloc.get(j).path.equals(path)) {
                            start = conAlloc.get(j).start;
                            length = conAlloc.get(j).length;
                            conAlloc.remove(j);
                            break;
                        }
                    }
                    for (int j = start; j < start + length; j++)
                        Blocks.set(j, "0");

                } else if (num == 2) {
                    for (int j = 0; j < IndAlloc.size(); j++) {
                        System.out.println(IndAlloc.get(j).path);
                        if (IndAlloc.get(j).path.equals(path)) {
                            Blocks.set(IndAlloc.get(j).index, "0");
                            for (int k = 0; k < IndAlloc.get(j).blocks.size(); k++)
                                Blocks.set(IndAlloc.get(j).blocks.get(k), "0");
                            IndAlloc.remove(j);
                            break;
                        }
                    }
                }
                break;
            }
        }
        if (check == false)
            System.out.println("File not found ");
    }

    public void DeleteFolder(String[] command) {
        boolean check = false;
        for (int i = 0; i < Directories.size(); i++) {
            if (Directories.get(i).contains(command[1] + "/") || Directories.get(i).equals(command[1])) {
                check = true;
                if (Directories.get(i).contains("."))
                    DeleteFile(Directories.get(i));
                else
                    Directories.remove(i);
                i--;
            }
        }
        if (check == false)
            System.out.println("folder not found in this directory");
    }

    public void DisplayDiskStatus() {
        System.out.println("Disk status ");
        System.out.println("-----------------------------------------------------------");
        int counter0 = 0;
        int counter1 = 0;
        for (int i = 0; i < Blocks.size(); i++) {
            if (Blocks.get(i).equals("0"))
                counter0++;
            else if (Blocks.get(i).equals("1"))
                counter1++;
        }
        System.out.println("Free space : " + counter0 + " KB");
        System.out.println("Allocated space : " + counter1 + " KB");
        System.out.println("Empty blocks on the disk : ");
        if (num == 1) {
            for (int j = 0; j < conAlloc.size(); j++) {
                int end = conAlloc.get(j).start + conAlloc.get(j).length;
                System.out.println("From : " + conAlloc.get(j).start + " To : " + end);
            }
        } else if (num == 2) {
            for (int i = 0; i < IndAlloc.size(); i++) {
                System.out.println("Block : " + IndAlloc.get(i).index);
                for (int j = 0; j < IndAlloc.get(i).blocks.size(); j++)
                    System.out.println("Block : " + IndAlloc.get(i).blocks.get(j));
            }
        }
    }

    public void DisplayDiskStructure() {
        System.out.println("Disk Structure ");
        System.out.println("-----------------------------------------------------------");
        for (int i = 0; i < Directories.size() ; i++)
            System.out.println(Directories.get(i));
    }

    public boolean ManageCommand(String com) {
        String[] command = com.split(" ", com.length() - 1);
        //CreateFile
        if (command[0].equals("CreateFile"))
            CreateFile(command, com);
            //CreateFolder
        else if (command[0].equals("CreateFolder"))
            CreateFolder(command, com);
            //DeleteFile
        else if (command[0].equals("DeleteFile"))
            DeleteFile(command[1]);
            //DeleteFolder
        else if (command[0].equals("DeleteFolder"))
            DeleteFolder(command);
            //DisplayDiskStatus
        else if (command[0].equals("DisplayDiskStatus"))
            DisplayDiskStatus();
            //DisplayDiskStructure
        else if (command[0].equals("DisplayDiskStructure"))
            DisplayDiskStructure();
        else
            return false;

        System.out.println("Blocks ");
        for (int i = 0; i < Blocks.size(); i++)
            System.out.print(Blocks.get(i));
        System.out.println("\n");
        System.out.println("---------------------------------------");
        System.out.println("\n");
        System.out.println("Directories : ");
        for (int j = 0; j < Directories.size(); j++)
            System.out.println(Directories.get(j));

        System.out.println("---------------------------------------");
        System.out.println("Contiguous allocation ");
        System.out.println(conAlloc.size());
        for (int i = 0; i < conAlloc.size(); i++) {
            System.out.println("directory ");
            for (int j = 0; j < conAlloc.get(i).dir.size(); j++)
                System.out.println(conAlloc.get(i).dir.get(j));

            System.out.println("Start " + conAlloc.get(i).start);

            System.out.println("length " + conAlloc.get(i).length);
        }
        System.out.println("---------------------------------------");
        System.out.println("Indexed : ");
        for (int i = 0; i < IndAlloc.size(); i++) {
            System.out.println("directory ");
            for (int j = 0; j < IndAlloc.get(i).dir.size(); j++)
                System.out.println(IndAlloc.get(i).dir.get(j));

            System.out.println("Index " + IndAlloc.get(i).index);

            System.out.println("Path : " + IndAlloc.get(i).path);
            System.out.println("Blocks ");
            for (int j = 0; j < IndAlloc.get(i).blocks.size(); j++)
                System.out.println(IndAlloc.get(i).blocks.get(j));

        }
       // saveToVF();
        return true;
    }

    public void saveToVF() {
        try {
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(Blocks.get(0));
            for (int i = 1; i < Blocks.size(); i++)
                writer.append(Blocks.get(i));
            writer.append("\n*\n");
            if(num == 1) {
                for (ContiguousAlloc contiguousAlloc : conAlloc) {
                    writer.append(contiguousAlloc.path).append(" ");
                    writer.append(String.valueOf(contiguousAlloc.start)).append(" ");
                    writer.append(String.valueOf(contiguousAlloc.length)).append(" , \n");
                }
            }
            else if(num == 2) {
                for (IndexedAlloc indexedAlloc : IndAlloc) {
                    writer.append(indexedAlloc.path).append(" ").append(String.valueOf(indexedAlloc.index)).append(" ");
                    for (int j = 0; j < indexedAlloc.blocks.size(); j++) {
                        writer.append(String.valueOf(indexedAlloc.blocks.get(j))).append(" ");
                    }
                    writer.append(", \n");
                }
            }

            writer.append('*');
            for (String directory : Directories) {
                writer.append('\n');
                writer.append(directory);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



