import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class ServerThread extends Thread{
    public Scanner scanner = new Scanner(System.in);

    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private Socket clientSocket = null;

    private Applicant applicant = null;
    private final StudentPortal studentPortal = new StudentPortal();
    private boolean adminAuthenticated = false;
    private boolean studentAuthenticated = false;
    private Applicant.Status status = null;
    private Admin admin = null;
    private String ext1,ext2,ext3;

    private String ack,response,password;
    private List<String> stats;
    private List<String> result;
    private int port;

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            port = clientSocket.getPort();
            System.out.println(ANSI.GREEN+"connected: "+clientSocket.getInetAddress()+":"+port+ANSI.RESET);
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            passDetails();
            homePage();
        }catch (EOFException | SocketException e){
            System.out.println(ANSI.RED+"["+port+"] 499 client closed request"+ANSI.RESET);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println(ANSI.YELLOW+"closed: "+clientSocket.getInetAddress()+":"+port+ANSI.RESET);
            try {
                clientSocket.close();
            }catch (IOException e){ }
        }
    }

    public void homePage() throws Exception{
        System.out.println("["+port+"] home/");
        int choice = dataInputStream.readInt();
        while (choice !=0){
            switch (choice){
                case 1:
                    System.out.println("\n"+"["+port+"] /admin-login");
                    String username = dataInputStream.readUTF();
                    System.out.println("["+port+"] username: "+username);
                    password = dataInputStream.readUTF();
                    System.out.println("["+port+"] password: "+ "*".repeat(password.length()));
                    admin = Admin.accessAdmin(username,password);
                    if(admin == null) {
                        dataOutputStream.writeInt(401);
                        System.out.println(ANSI.RED+"["+port+"] 401 unauthorized"+ANSI.RESET);
                    }
                    else {
                        dataOutputStream.writeInt(202);
                        System.out.println(ANSI.GREEN+"["+port+"] 202 accepted"+ANSI.RESET);
                        adminView(SessionHandler.getOrCreateAdminSession(admin.getUsername()));
                    }
                    break;
                case 2:
                    System.out.println("\n"+"["+port+"] /applicant-login");
                    String id = dataInputStream.readUTF();
                    System.out.println("["+port+"] id: "+id);
                    password = dataInputStream.readUTF();
                    System.out.println("["+port+"] password: "+ "*".repeat(password.length()));
                    applicant = studentPortal.fetchApplicant(id,password);
                    if(applicant == null) {
                        dataOutputStream.writeInt(401);
                        System.out.println(ANSI.RED+"["+port+"] 401 unauthorized"+ANSI.RESET);
                    }
                    else {
                        dataOutputStream.writeInt(202);
                        System.out.println(ANSI.GREEN+"["+port+"] 202 accepted"+ANSI.RESET);
                        applicantView(SessionHandler.getOrCreateApplicantSession(applicant.getApplicationId()));
                    }
                break;

                case 3:
                    System.out.println("\n"+"["+port+"] /applicant-registration");
                    applicantRegistration();
                    String applicantId = studentPortal.register();
                    if(applicantId == null) {
                        dataOutputStream.writeInt(400);
                        System.out.println(ANSI.RED+"["+port+"] 400 bad request"+ANSI.RESET);
                    }
                    else {
                        File oldFile,newFile;
                        String uniqueId = studentPortal.uniqueId;

                        oldFile = new File("media/temp_"+uniqueId+"_photograph."+ext1);
                        newFile = new File("media/"+applicantId+"_photograph."+ext1);
                        oldFile.renameTo(newFile);

                        oldFile = new File("media/temp_"+uniqueId+"_signature."+ext2);
                        newFile = new File("media/"+applicantId+"_signature."+ext2);
                        oldFile.renameTo(newFile);

                        oldFile = new File("media/temp_"+uniqueId+"_id_proof."+ext3);
                        newFile = new File("media/"+applicantId+"_id_proof."+ext3);
                        oldFile.renameTo(newFile);

                        dataOutputStream.writeInt(201);
                        System.out.println(ANSI.GREEN+"["+port+"] 201 created"+ANSI.RESET);
                        dataOutputStream.writeUTF(applicantId);
                    }
                break;

                case 4:
                    helpCenter();
                break;

                default:
                    System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                    dataOutputStream.writeUTF("Invalid Selection");
            }
            choice = dataInputStream.readInt();
        }
    }

    public void applicantView(Session session) throws Exception{
        Lock lock = session.getLock();
        if(lock.tryLock(5000L, TimeUnit.MILLISECONDS)){
            dataOutputStream.writeInt(200);
            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
            System.out.println("\n"+"["+port+"] applicants-page/");
            SerializedApplicant serializedApplicant = new SerializedApplicant(applicant);
            objectOutputStream.writeObject(serializedApplicant);

            int choice = dataInputStream.readInt();
            session.updateLastActivity();
            while (choice!=0){
                switch (choice){
                    case 1:
                        System.out.println("["+port+"] /float-seat");
                        status = applicant.hover();
                        if(status != Applicant.Status.FLOATED) {
                            dataOutputStream.writeInt(403);
                            System.out.println(ANSI.RED+"["+port+"] 403 forbidden"+ANSI.RESET);
                        }
                        else {
                            dataOutputStream.writeInt(200);
                            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        }
                        break;

                    case 2:
                        System.out.println("["+port+"] /lock-seat");
                        status = applicant.lock();
                        if(status != Applicant.Status.LOCKED) {
                            dataOutputStream.writeInt(403);
                            System.out.println(ANSI.RED+"["+port+"] 403 forbidden"+ANSI.RESET);
                        }
                        else {
                            dataOutputStream.writeInt(200);
                            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        }
                        break;

                    case 3:
                        System.out.println("["+port+"] /fill-enrollment-details");
                        boolean flag = fillEnrollmentForm();
                        if(studentPortal.submitEnrollmentForm(applicant)) {
                            dataOutputStream.writeInt(202);
                            System.out.println(ANSI.GREEN+"["+port+"] 202 accepted"+ANSI.RESET);
                        }
                        else {
                            dataOutputStream.writeInt(400);
                            System.out.println(ANSI.RED+"["+port+"] 400 bad request"+ANSI.RESET);
                        }
                        break;

                    case 4:
                        String password = applicant.getPassword();
                        String id = applicant.getApplicationId();
                        applicant = studentPortal.fetchApplicant(id,password);
                        serializedApplicant = new SerializedApplicant(applicant);
                        objectOutputStream.writeObject(serializedApplicant);
                    break;

                    case 5:
                        String currentPassword = dataInputStream.readUTF();
                        String newPassword = dataInputStream.readUTF();
                        if(applicant.matchPassword(currentPassword)) {
                            if (newPassword.length() < 8) {
                                dataOutputStream.writeInt(400);
                                System.out.println(ANSI.RED + "[" + port + "] 400 bad request" + ANSI.RESET);
                            } else {
                                applicant.updatePassword(newPassword);
                                dataOutputStream.writeInt(200);
                                System.out.println(ANSI.RED + "[" + port + "] 200 ok" + ANSI.RESET);
                            }
                        } else {
                            dataOutputStream.writeInt(401);
                            System.out.println(ANSI.RED + "[" + port + "] 401 unauthorized" + ANSI.RESET);
                        }
                    break;

                    case 6:
                        sendFile("assets/EnrollmentForm.pdf");
                    break;

                    default:
                        System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                        dataOutputStream.writeUTF("Invalid Selection");
                }
                choice = dataInputStream.readInt();
                session.updateLastActivity();
            }
            System.out.println("\n"+"["+port+"] applicants-portal/");
            lock.unlock();
            SessionHandler.deleteApplicantSession(applicant.getApplicationId());
        }else {
            dataOutputStream.writeInt(409);
            System.out.println(ANSI.RED+"["+port+"] 409 conflict"+ANSI.RESET);
        }
    }

    public void adminView(Session session) throws Exception {
        Lock lock = session.getLock();
        if(lock.tryLock(5000L,TimeUnit.MILLISECONDS)){
            dataOutputStream.writeInt(200);
            System.out.println("\n"+"["+port+"] admins-page/");
            dataOutputStream.writeUTF(admin.getName());
            stats = admin.getStats();
            dataOutputStream.writeInt(stats.size());
            for(String string : stats)
                dataOutputStream.writeUTF(string);

            int choice = dataInputStream.readInt();
            session.updateLastActivity();
            while (choice != 0){
                switch (choice){
                    case 1:
                        System.out.println("["+port+"] /list-applicants");
                        String status = dataInputStream.readUTF();
                        String sql = String.format("select applicant_id from applicant where status like '%s'",status);
                        result = admin.getList(sql);
                        dataOutputStream.writeInt(result.size());
                        for (String string: result)
                            dataOutputStream.writeUTF(string);
                        System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        break;

                    case 2:
                        String glob = dataInputStream.readUTF();
                        String path = findFile("media/",glob);
                        if(path == null) {
                            dataOutputStream.writeInt(404);
                            System.out.println(ANSI.GREEN + "[" + port + "] 404 not found" + ANSI.RESET);
                        }else {
                            dataOutputStream.writeInt(200);
                            String extension = path.substring(path.lastIndexOf('.') + 1);
                            dataOutputStream.writeUTF(extension);
                            sendFile(path);
                            System.out.println(ANSI.GREEN + "[" + port + "] 200 ok" + ANSI.RESET);
                        }
                        break;

                    case 3:
                        System.out.println("["+port+"] /shortlist");
                        admin.shortlistApplicants();
                        dataOutputStream.writeInt(200);
                        System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        break;

                    case 4:
                        System.out.println("["+port+"] /check-status");
                        String id = dataInputStream.readUTF();
                        applicant = admin.fetchApplicant(id);
                        if(applicant!=null) {
                            dataOutputStream.writeInt(200);
                            SerializedApplicant serializedApplicant = new SerializedApplicant(applicant);
                            objectOutputStream.writeObject(serializedApplicant);
                            System.out.println(ANSI.GREEN + "[" + port + "] 200 ok" + ANSI.RESET);
                        }else{
                            dataOutputStream.writeInt(404);
                            System.out.println(ANSI.GREEN + "[" + port + "] 404 not found" + ANSI.RESET);
                        }
                        break;

                    case 5:
                        System.out.println("["+port+"] /register-admin");
                        String username = dataInputStream.readUTF();
                        System.out.println("["+port+"] username: "+username);
                        String name = dataInputStream.readUTF();
                        System.out.println("["+port+"] name: "+name);
                        String password = dataInputStream.readUTF();
                        System.out.println("["+port+"] password: "+ "*".repeat(password.length()));
                        if(password.length()<8){
                            dataOutputStream.writeInt(400);
                            System.out.println(ANSI.RED+"["+port+"] 400 bad request"+ANSI.RESET);
                        }
                        else {
                            if(admin.registerNewAdmin(username,name,password)) {
                                dataOutputStream.writeInt(201);
                                System.out.println(ANSI.GREEN + "[" + port + "] 201 created" + ANSI.RESET);
                            }else {
                                dataOutputStream.writeInt(409);
                                System.out.println(ANSI.GREEN + "[" + port + "] 409 conflict" + ANSI.RESET);
                            }
                        }
                        break;

                    case 6:
                        System.out.println("["+port+"] /issue-enrollment-forms");
                        admin.issueEnrollmentForms();
                        dataOutputStream.writeInt(200);
                        System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        break;

                    case 7:
                        System.out.println("["+port+"] /view-stats");
                        stats = admin.getStats();
                        dataOutputStream.writeInt(stats.size());
                        for(String string : stats)
                            dataOutputStream.writeUTF(string);
                        System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        break;

                    case 8:
                        System.out.println("["+port+"] /list-enrollment-forms");
                        result = admin.getEnrollmentForms();
                        dataOutputStream.writeInt(result.size());
                        for(String string : result) {
                            dataOutputStream.writeUTF(string);
                            System.out.println(string);
                        }
                        System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        break;

                    case 9:
                        System.out.println("["+port+"] /enroll");
                        String applicantId = dataInputStream.readUTF();
                        if(admin.enrollApplicant(applicantId)) {
                            dataOutputStream.writeInt(200);
                            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        }
                        else {
                            dataOutputStream.writeInt(403);
                            System.out.println(ANSI.RED+"["+port+"] 403 forbidden"+ANSI.RESET);
                        }
                        break;

                    case 10:
                        System.out.println("["+port+"] /disqualify");
                        applicantId = dataInputStream.readUTF();
                        if(admin.disqualifyApplicant(applicantId)) {
                            dataOutputStream.writeInt(200);
                            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        }
                        else {
                            dataOutputStream.writeInt(403);
                            System.out.println(ANSI.RED+"["+port+"] 403 forbidden"+ANSI.RESET);
                        }
                    break;

                    case 11:
                        support(session);
                        break;

                    case 12:
                        University.email = dataInputStream.readUTF();
                        University.contact = dataInputStream.readUTF();
                        University.entrance = dataInputStream.readUTF();
                        University.maxMarks = dataInputStream.readDouble();
                        receiveAsset("banner.png");
                        break;

                    case 13:
                        String branchName = dataInputStream.readUTF();
                        int seats = dataInputStream.readInt();
                        int cutOff = dataInputStream.readInt();
                        University.Branch branch = new University.Branch(branchName,seats,cutOff);
                        University.addBranch(branch);
                        break;

                    case 14:
                        branchName = dataInputStream.readUTF();
                        branch = new University.Branch(branchName);
                        if(University.branches.contains(branch)){
                            University.branches.remove(branch);
                            dataOutputStream.writeInt(200);
                            System.out.println(ANSI.GREEN+"["+port+"] 200 ok"+ANSI.RESET);
                        }else{
                            dataOutputStream.writeInt(404);
                            System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                        }
                        break;

                    case 15:
                        receiveAsset("EnrollmentForm.pdf");
                    break;

                    case 16:
                        String event = dataInputStream.readUTF();
                        String date = dataInputStream.readUTF();
                        University.addEvent(event,date);
                    break;

                    default:
                        System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                        dataOutputStream.writeUTF("Invalid Selection");
                }
                choice = dataInputStream.readInt();
                session.updateLastActivity();
            }
            System.out.println("\n"+"["+port+"] home/");
            lock.unlock();
            SessionHandler.deleteAdminSession(admin.getUsername());
        }
        else{
            ack="409 conflict";
            System.out.println(ANSI.RED+"["+port+"] "+ack+ANSI.RESET);
            dataOutputStream.writeUTF(ack);
        }
    }

    public void support(Session session) throws Exception{
        Support support = null;
        String ticketNo;
        List<String> conversation;
        List<String> tickets = admin.getTickets();
        dataOutputStream.writeInt(tickets.size());
        for(String ticket : tickets)
            dataOutputStream.writeUTF(ticket);
        System.out.println("\n"+"["+port+"] support/");
//        dataOutputStream.writeUTF(admin.listQueries());
        int choice = dataInputStream.readInt();
        while (choice != 0){
            switch (choice){
                case 1:
                    ticketNo = dataInputStream.readUTF();
                    session.updateLastActivity();
                    support = Database.getSupportObject(ticketNo);
                    if(support == null) support = new Support();
                    dataOutputStream.writeUTF(support.getClientName());
                    conversation = support.getConversation();
                    dataOutputStream.writeInt(conversation.size());
                    for(String message : conversation)
                        dataOutputStream.writeUTF(message);
                break;

                case 2:
                    String message = dataInputStream.readUTF();
                    session.updateLastActivity();
                    if(support.getAdminUsername() == null)
                        support.setAdminUsername(admin.getUsername());
                    support.post(message,true);
                break;

                case 3:
                    tickets = admin.getTickets();
                    dataOutputStream.writeInt(tickets.size());
                    for(String ticket : tickets)
                        dataOutputStream.writeUTF(ticket);
                break;

                case 4:
                    conversation = support.getConversation();
                    dataOutputStream.writeInt(conversation.size());
                    for(String text : conversation)
                        dataOutputStream.writeUTF(text);
                break;

                default:
                    System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                    dataOutputStream.writeUTF("Invalid Selection");
            }
            choice = dataInputStream.readInt();
        }
        System.out.println("\n"+"["+port+"] admins-page/");
    }

    public void helpCenter() throws Exception{
        System.out.println("\n"+"["+port+"] help-center/");
        int choice = dataInputStream.readInt();
        while (choice!=0){
            switch (choice){
                case 1:
                    newSupportTicket();
                break;

                case 2:
                    String ticketNo = dataInputStream.readUTF();
                    Support support = Database.getSupportObject(ticketNo);
                    if(support==null){
                        dataOutputStream.writeInt(404);
                        System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                    }
                    else {
                        dataOutputStream.writeInt(200);
                        System.out.println(ANSI.RED+"["+port+"] 200 ok"+ANSI.RESET);
                        existingSupportTicket(support);
                    }
                break;

                default:
                    System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                    dataOutputStream.writeUTF("Invalid Selection");

            }
            choice = dataInputStream.readInt();
        }

        System.out.println("["+port+"] home/");
    }

    public void newSupportTicket() throws Exception{
        Support support = new Support();
        boolean isRegistered = dataInputStream.readBoolean();
        if(isRegistered){
            String applicantId = dataInputStream.readUTF();
            boolean validApplicantId = support.setApplicantId(applicantId);
            if(validApplicantId){
                dataOutputStream.writeInt(200);
            }else{
                dataOutputStream.writeInt(404);
                return;
            }
        }
        else
            support.setClientName(dataInputStream.readUTF());

        support.generateTicketNo();
        Database.addSupportObject(support);
        dataOutputStream.writeUTF(support.getTicketNo());
        existingSupportTicket(support);
    }
    public void existingSupportTicket(Support support) throws Exception{
        List<String> conversation = support.getConversation();
        dataOutputStream.writeBoolean(support.isResolved());
        if(support.getAdminUsername() == null)
            dataOutputStream.writeUTF("None");
        else
            dataOutputStream.writeUTF(support.getAdminUsername());
        dataOutputStream.writeInt(conversation.size());
        for(String message: conversation)
            dataOutputStream.writeUTF(message);

        int choice = dataInputStream.readInt();
        while (choice!=0){
            switch (choice){
                case 1:
                    String message = dataInputStream.readUTF();
                    support.post(message,false);
                break;

                case 2:
                    if(!support.isResolved())
                        support.setResolved(true);
                break;

                case 3:
                    if(support.getAdminUsername() == null)
                        dataOutputStream.writeUTF("None");
                    else
                        dataOutputStream.writeUTF(support.getAdminUsername());
                    conversation = support.getConversation();
                    dataOutputStream.writeInt(conversation.size());
                    for(String text: conversation)
                        dataOutputStream.writeUTF(text);
                break;

                default:
                    System.out.println(ANSI.RED+"["+port+"] 404 not found"+ANSI.RESET);
                    dataOutputStream.writeUTF("Invalid Selection");
            }
            choice = dataInputStream.readInt();
        }
    }

    public void applicantRegistration() throws Exception{
        SerializedApplicationForm applicationForm = new SerializedApplicationForm();
        applicationForm.setExam(University.entrance);
        objectOutputStream.writeObject(applicationForm);
        applicationForm = (SerializedApplicationForm) objectInputStream.readObject();

        studentPortal.firstName = applicationForm.getFirstName();
        studentPortal.lastName = applicationForm.getLastName();
        studentPortal.uniqueId = applicationForm.getUniqueId();
        studentPortal.email = applicationForm.getEmail();
        studentPortal.phNo = applicationForm.getPhNo();
        studentPortal.password = applicationForm.getPassword();
        studentPortal.regNo = applicationForm.getRegNo();
        studentPortal.obtainedMarks = applicationForm.getObtainedMarks();
        studentPortal.percentile = applicationForm.getPercentile();
        studentPortal.hscBoard = applicationForm.getHscBoard();
        studentPortal.hscReg = applicationForm.getHscRegNo();
        studentPortal.hscPercentage = applicationForm.getHscPercentage();
        studentPortal.branchName = applicationForm.getBranchName();

        // accept files from applicant
        ext1 = dataInputStream.readUTF();
        ext2 = dataInputStream.readUTF();
        ext3 = dataInputStream.readUTF();
        receiveFile("temp_"+studentPortal.uniqueId+"_photograph."+ext1);
        receiveFile("temp_"+studentPortal.uniqueId+"_signature."+ext2);
        receiveFile("temp_"+studentPortal.uniqueId+"_id_proof."+ext3);

        System.out.println(ANSI.CYAN+applicationForm+ANSI.RESET);
    }

    public boolean fillEnrollmentForm() throws Exception{

        try{
            receiveFile(applicant.getApplicationId()+"_form.pdf");
            receiveFile(applicant.getApplicationId()+"_entrance.pdf");
            receiveFile(applicant.getApplicationId()+"_hsc.pdf");
            studentPortal.form = "media/"+applicant.getApplicationId()+"_form.pdf";
            studentPortal.entranceMarkSheet = "media/"+applicant.getApplicationId()+"_entrance.pdf";
            studentPortal.hscMarkSheet = "media/"+applicant.getApplicationId()+"_hsc.pdf";
            return true;
        } catch (Exception e){
            studentPortal.form = "EMPTY";
            studentPortal.entranceMarkSheet = "EMPTY";
            studentPortal.hscMarkSheet = "EMPTY";
            return false;
        }
    }

    public void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        dataOutputStream.writeLong(file.length());
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }

    private void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream("media/"+fileName);

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;
        }
        fileOutputStream.close();
    }

    private void receiveAsset(String fileName) throws Exception{
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream("assets/"+fileName);

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;
        }
        fileOutputStream.close();
    }

    public String findFile(String dir, String glob){
        try(
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), glob)
        ){
            return dirStream.iterator().next().toAbsolutePath().toString();
        }catch (Exception e){
            return null;
        }
    }

    public void passDetails() throws Exception{
        dataOutputStream.writeUTF(University.email);
        dataOutputStream.writeUTF(University.contact);
        dataOutputStream.writeUTF(University.entrance);
        dataOutputStream.writeDouble(University.maxMarks);

        int n = University.branches.size();
        dataOutputStream.writeInt(n);
        for(University.Branch branch : University.branches)
            dataOutputStream.writeUTF(branch.getName());

        List<String> events = University.getEvents();
        n = events.size();
        dataOutputStream.writeInt(n);
        for(String event : events)
            dataOutputStream.writeUTF(event);

        sendFile("assets/banner.png");
    }
}
