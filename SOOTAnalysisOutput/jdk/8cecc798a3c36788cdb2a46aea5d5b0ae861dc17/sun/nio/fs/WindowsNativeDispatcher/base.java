package sun.nio.fs;

import java.security.AccessController;
import java.security.PrivilegedAction;
import jdk.internal.misc.Unsafe;
import static sun.nio.fs.WindowsConstants.*;

class WindowsNativeDispatcher {

    private WindowsNativeDispatcher() {
    }

    static native long CreateEvent(boolean bManualReset, boolean bInitialState) throws WindowsException;

    static long CreateFile(String path, int dwDesiredAccess, int dwShareMode, long lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return CreateFile0(buffer.address(), dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes);
        } finally {
            buffer.release();
        }
    }

    static long CreateFile(String path, int dwDesiredAccess, int dwShareMode, int dwCreationDisposition, int dwFlagsAndAttributes) throws WindowsException {
        return CreateFile(path, dwDesiredAccess, dwShareMode, 0L, dwCreationDisposition, dwFlagsAndAttributes);
    }

    private static native long CreateFile0(long lpFileName, int dwDesiredAccess, int dwShareMode, long lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes) throws WindowsException;

    static native void CloseHandle(long handle);

    static void DeleteFile(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            DeleteFile0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native void DeleteFile0(long lpFileName) throws WindowsException;

    static void CreateDirectory(String path, long lpSecurityAttributes) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            CreateDirectory0(buffer.address(), lpSecurityAttributes);
        } finally {
            buffer.release();
        }
    }

    private static native void CreateDirectory0(long lpFileName, long lpSecurityAttributes) throws WindowsException;

    static void RemoveDirectory(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            RemoveDirectory0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native void RemoveDirectory0(long lpFileName) throws WindowsException;

    static native void DeviceIoControlSetSparse(long handle) throws WindowsException;

    static native void DeviceIoControlGetReparsePoint(long handle, long bufferAddress, int bufferSize) throws WindowsException;

    static FirstFile FindFirstFile(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            FirstFile data = new FirstFile();
            FindFirstFile0(buffer.address(), data);
            return data;
        } finally {
            buffer.release();
        }
    }

    static class FirstFile {

        private long handle;

        private String name;

        private int attributes;

        private FirstFile() {
        }

        public long handle() {
            return handle;
        }

        public String name() {
            return name;
        }

        public int attributes() {
            return attributes;
        }
    }

    private static native void FindFirstFile0(long lpFileName, FirstFile obj) throws WindowsException;

    static long FindFirstFile(String path, long address) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return FindFirstFile1(buffer.address(), address);
        } finally {
            buffer.release();
        }
    }

    private static native long FindFirstFile1(long lpFileName, long address) throws WindowsException;

    static native String FindNextFile(long handle, long address) throws WindowsException;

    static FirstStream FindFirstStream(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            FirstStream data = new FirstStream();
            FindFirstStream0(buffer.address(), data);
            if (data.handle() == WindowsConstants.INVALID_HANDLE_VALUE)
                return null;
            return data;
        } finally {
            buffer.release();
        }
    }

    static class FirstStream {

        private long handle;

        private String name;

        private FirstStream() {
        }

        public long handle() {
            return handle;
        }

        public String name() {
            return name;
        }
    }

    private static native void FindFirstStream0(long lpFileName, FirstStream obj) throws WindowsException;

    static native String FindNextStream(long handle) throws WindowsException;

    static native void FindClose(long handle) throws WindowsException;

    static native void GetFileInformationByHandle(long handle, long address) throws WindowsException;

    static void CopyFileEx(String source, String target, int flags, long addressToPollForCancel) throws WindowsException {
        NativeBuffer sourceBuffer = asNativeBuffer(source);
        NativeBuffer targetBuffer = asNativeBuffer(target);
        try {
            CopyFileEx0(sourceBuffer.address(), targetBuffer.address(), flags, addressToPollForCancel);
        } finally {
            targetBuffer.release();
            sourceBuffer.release();
        }
    }

    private static native void CopyFileEx0(long existingAddress, long newAddress, int flags, long addressToPollForCancel) throws WindowsException;

    static void MoveFileEx(String source, String target, int flags) throws WindowsException {
        NativeBuffer sourceBuffer = asNativeBuffer(source);
        NativeBuffer targetBuffer = asNativeBuffer(target);
        try {
            MoveFileEx0(sourceBuffer.address(), targetBuffer.address(), flags);
        } finally {
            targetBuffer.release();
            sourceBuffer.release();
        }
    }

    private static native void MoveFileEx0(long existingAddress, long newAddress, int flags) throws WindowsException;

    static int GetFileAttributes(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return GetFileAttributes0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native int GetFileAttributes0(long lpFileName) throws WindowsException;

    static void SetFileAttributes(String path, int dwFileAttributes) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            SetFileAttributes0(buffer.address(), dwFileAttributes);
        } finally {
            buffer.release();
        }
    }

    private static native void SetFileAttributes0(long lpFileName, int dwFileAttributes) throws WindowsException;

    static void GetFileAttributesEx(String path, long address) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            GetFileAttributesEx0(buffer.address(), address);
        } finally {
            buffer.release();
        }
    }

    private static native void GetFileAttributesEx0(long lpFileName, long address) throws WindowsException;

    static native void SetFileTime(long handle, long createTime, long lastAccessTime, long lastWriteTime) throws WindowsException;

    static native void SetEndOfFile(long handle) throws WindowsException;

    static native int GetLogicalDrives() throws WindowsException;

    static VolumeInformation GetVolumeInformation(String root) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(root);
        try {
            VolumeInformation info = new VolumeInformation();
            GetVolumeInformation0(buffer.address(), info);
            return info;
        } finally {
            buffer.release();
        }
    }

    static class VolumeInformation {

        private String fileSystemName;

        private String volumeName;

        private int volumeSerialNumber;

        private int flags;

        private VolumeInformation() {
        }

        public String fileSystemName() {
            return fileSystemName;
        }

        public String volumeName() {
            return volumeName;
        }

        public int volumeSerialNumber() {
            return volumeSerialNumber;
        }

        public int flags() {
            return flags;
        }
    }

    private static native void GetVolumeInformation0(long lpRoot, VolumeInformation obj) throws WindowsException;

    static int GetDriveType(String root) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(root);
        try {
            return GetDriveType0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native int GetDriveType0(long lpRoot) throws WindowsException;

    static DiskFreeSpace GetDiskFreeSpaceEx(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            DiskFreeSpace space = new DiskFreeSpace();
            GetDiskFreeSpaceEx0(buffer.address(), space);
            return space;
        } finally {
            buffer.release();
        }
    }

    static DiskFreeSpace GetDiskFreeSpace(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            DiskFreeSpace space = new DiskFreeSpace();
            GetDiskFreeSpace0(buffer.address(), space);
            return space;
        } finally {
            buffer.release();
        }
    }

    static class DiskFreeSpace {

        private long freeBytesAvailable;

        private long totalNumberOfBytes;

        private long totalNumberOfFreeBytes;

        private long bytesPerSector;

        private DiskFreeSpace() {
        }

        public long freeBytesAvailable() {
            return freeBytesAvailable;
        }

        public long totalNumberOfBytes() {
            return totalNumberOfBytes;
        }

        public long totalNumberOfFreeBytes() {
            return totalNumberOfFreeBytes;
        }

        public long bytesPerSector() {
            return bytesPerSector;
        }
    }

    private static native void GetDiskFreeSpaceEx0(long lpDirectoryName, DiskFreeSpace obj) throws WindowsException;

    private static native void GetDiskFreeSpace0(long lpRootPathName, DiskFreeSpace obj) throws WindowsException;

    static String GetVolumePathName(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return GetVolumePathName0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native String GetVolumePathName0(long lpFileName) throws WindowsException;

    static native void InitializeSecurityDescriptor(long sdAddress) throws WindowsException;

    static native void InitializeAcl(long aclAddress, int size) throws WindowsException;

    static int GetFileSecurity(String path, int requestedInformation, long pSecurityDescriptor, int nLength) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return GetFileSecurity0(buffer.address(), requestedInformation, pSecurityDescriptor, nLength);
        } finally {
            buffer.release();
        }
    }

    private static native int GetFileSecurity0(long lpFileName, int requestedInformation, long pSecurityDescriptor, int nLength) throws WindowsException;

    static void SetFileSecurity(String path, int securityInformation, long pSecurityDescriptor) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            SetFileSecurity0(buffer.address(), securityInformation, pSecurityDescriptor);
        } finally {
            buffer.release();
        }
    }

    static native void SetFileSecurity0(long lpFileName, int securityInformation, long pSecurityDescriptor) throws WindowsException;

    static native long GetSecurityDescriptorOwner(long pSecurityDescriptor) throws WindowsException;

    static native void SetSecurityDescriptorOwner(long pSecurityDescriptor, long pOwner) throws WindowsException;

    static native long GetSecurityDescriptorDacl(long pSecurityDescriptor);

    static native void SetSecurityDescriptorDacl(long pSecurityDescriptor, long pAcl) throws WindowsException;

    static AclInformation GetAclInformation(long aclAddress) {
        AclInformation info = new AclInformation();
        GetAclInformation0(aclAddress, info);
        return info;
    }

    static class AclInformation {

        private int aceCount;

        private AclInformation() {
        }

        public int aceCount() {
            return aceCount;
        }
    }

    private static native void GetAclInformation0(long aclAddress, AclInformation obj);

    static native long GetAce(long aclAddress, int aceIndex);

    static native void AddAccessAllowedAceEx(long aclAddress, int flags, int mask, long sidAddress) throws WindowsException;

    static native void AddAccessDeniedAceEx(long aclAddress, int flags, int mask, long sidAddress) throws WindowsException;

    static Account LookupAccountSid(long sidAddress) throws WindowsException {
        Account acc = new Account();
        LookupAccountSid0(sidAddress, acc);
        return acc;
    }

    static class Account {

        private String domain;

        private String name;

        private int use;

        private Account() {
        }

        public String domain() {
            return domain;
        }

        public String name() {
            return name;
        }

        public int use() {
            return use;
        }
    }

    private static native void LookupAccountSid0(long sidAddress, Account obj) throws WindowsException;

    static int LookupAccountName(String accountName, long pSid, int cbSid) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(accountName);
        try {
            return LookupAccountName0(buffer.address(), pSid, cbSid);
        } finally {
            buffer.release();
        }
    }

    private static native int LookupAccountName0(long lpAccountName, long pSid, int cbSid) throws WindowsException;

    static native int GetLengthSid(long sidAddress);

    static native String ConvertSidToStringSid(long sidAddress) throws WindowsException;

    static long ConvertStringSidToSid(String sidString) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(sidString);
        try {
            return ConvertStringSidToSid0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native long ConvertStringSidToSid0(long lpStringSid) throws WindowsException;

    static native long GetCurrentProcess();

    static native long GetCurrentThread();

    static native long OpenProcessToken(long hProcess, int desiredAccess) throws WindowsException;

    static native long OpenThreadToken(long hThread, int desiredAccess, boolean openAsSelf) throws WindowsException;

    static native long DuplicateTokenEx(long hThread, int desiredAccess) throws WindowsException;

    static native void SetThreadToken(long thread, long hToken) throws WindowsException;

    static native int GetTokenInformation(long token, int tokenInfoClass, long pTokenInfo, int tokenInfoLength) throws WindowsException;

    static native void AdjustTokenPrivileges(long token, long luid, int attributes) throws WindowsException;

    static native boolean AccessCheck(long token, long securityInfo, int accessMask, int genericRead, int genericWrite, int genericExecute, int genericAll) throws WindowsException;

    static long LookupPrivilegeValue(String name) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(name);
        try {
            return LookupPrivilegeValue0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native long LookupPrivilegeValue0(long lpName) throws WindowsException;

    static void CreateSymbolicLink(String link, String target, int flags) throws WindowsException {
        NativeBuffer linkBuffer = asNativeBuffer(link);
        NativeBuffer targetBuffer = asNativeBuffer(target);
        try {
            CreateSymbolicLink0(linkBuffer.address(), targetBuffer.address(), flags);
        } catch (WindowsException x) {
            if (x.lastError() == ERROR_PRIVILEGE_NOT_HELD) {
                flags |= SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE;
                try {
                    CreateSymbolicLink0(linkBuffer.address(), targetBuffer.address(), flags);
                    return;
                } catch (WindowsException ignored) {
                }
            }
            throw x;
        } finally {
            targetBuffer.release();
            linkBuffer.release();
        }
    }

    private static native void CreateSymbolicLink0(long linkAddress, long targetAddress, int flags) throws WindowsException;

    static void CreateHardLink(String newFile, String existingFile) throws WindowsException {
        NativeBuffer newFileBuffer = asNativeBuffer(newFile);
        NativeBuffer existingFileBuffer = asNativeBuffer(existingFile);
        try {
            CreateHardLink0(newFileBuffer.address(), existingFileBuffer.address());
        } finally {
            existingFileBuffer.release();
            newFileBuffer.release();
        }
    }

    private static native void CreateHardLink0(long newFileBuffer, long existingFileBuffer) throws WindowsException;

    static String GetFullPathName(String path) throws WindowsException {
        NativeBuffer buffer = asNativeBuffer(path);
        try {
            return GetFullPathName0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native String GetFullPathName0(long pathAddress) throws WindowsException;

    static native String GetFinalPathNameByHandle(long handle) throws WindowsException;

    static native String FormatMessage(int errorCode);

    static native void LocalFree(long address);

    static native long CreateIoCompletionPort(long fileHandle, long existingPort, long completionKey) throws WindowsException;

    static CompletionStatus GetQueuedCompletionStatus(long completionPort) throws WindowsException {
        CompletionStatus status = new CompletionStatus();
        GetQueuedCompletionStatus0(completionPort, status);
        return status;
    }

    static class CompletionStatus {

        private int error;

        private int bytesTransferred;

        private long completionKey;

        private CompletionStatus() {
        }

        int error() {
            return error;
        }

        int bytesTransferred() {
            return bytesTransferred;
        }

        long completionKey() {
            return completionKey;
        }
    }

    private static native void GetQueuedCompletionStatus0(long completionPort, CompletionStatus status) throws WindowsException;

    static native void PostQueuedCompletionStatus(long completionPort, long completionKey) throws WindowsException;

    static native void ReadDirectoryChangesW(long hDirectory, long bufferAddress, int bufferLength, boolean watchSubTree, int filter, long bytesReturnedAddress, long pOverlapped) throws WindowsException;

    static native void CancelIo(long hFile) throws WindowsException;

    static native int GetOverlappedResult(long hFile, long lpOverlapped) throws WindowsException;

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    static NativeBuffer asNativeBuffer(String s) {
        int stringLengthInBytes = s.length() << 1;
        int sizeInBytes = stringLengthInBytes + 2;
        NativeBuffer buffer = NativeBuffers.getNativeBufferFromCache(sizeInBytes);
        if (buffer == null) {
            buffer = NativeBuffers.allocNativeBuffer(sizeInBytes);
        } else {
            if (buffer.owner() == s)
                return buffer;
        }
        char[] chars = s.toCharArray();
        unsafe.copyMemory(chars, Unsafe.ARRAY_CHAR_BASE_OFFSET, null, buffer.address(), (long) stringLengthInBytes);
        unsafe.putChar(buffer.address() + stringLengthInBytes, (char) 0);
        buffer.setOwner(s);
        return buffer;
    }

    private static native void initIDs();

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("net");
                System.loadLibrary("nio");
                return null;
            }
        });
        initIDs();
    }
}
