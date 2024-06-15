package finalCampaign.patch;

// from github repo crimsonwoods/javassist-android

import java.io.*;
import com.android.dx.dex.*;
import com.android.dx.dex.cf.*;
import com.android.dx.dex.file.*;

public class dexFile {
	private final com.android.dx.dex.file.DexFile file;
	private final DexOptions dex_options = new DexOptions();
	
	public dexFile() {
		this.file = new com.android.dx.dex.file.DexFile(dex_options);
	}
	
	public void addClass(String className, byte[] byteCode) {
		CfOptions cf_options = new CfOptions();
        cf_options.strictNameCheck = false;
		final ClassDefItem cdi = CfTranslator.translate(className, byteCode, cf_options, dex_options);
		file.add(cdi);
	}

	public byte[] toByte() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
		file.writeTo(stream, null, false);
		return stream.toByteArray();
	}
	
}