package org.mybatis.jpetstore.web.actions;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.mybatis.jpetstore.configuration.AWSS3;
import org.mybatis.jpetstore.domain.AnimalMating;
import org.mybatis.jpetstore.service.AnimalService;
import org.mybatis.jpetstore.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SessionScope
public class AnimalActionBean extends AbstractActionBean {
    private static final long serialVersionUID = -6687732592582712578L;

    private static final String ADD_ANIMAL_MATING="/WEB-INF/jsp/animalmating/AddAnimalForm.jsp";
    private static final String LIST_ANIMAL_MATING="/WEB-INF/jsp/animalmating/ListAnimalMating.jsp";
    private static final String DETAIL_ANIMAL_MATING="/WEB-INF/jsp/animalmating/DetailAnimalMating.jsp";
    private static final List<String> CATEGORY_LIST;
    private static final List<String> SEX_LIST;

    private static List<String> searchOptionList;
    private String searchOption;


    @SpringBean
    private transient AnimalService animalService;
    @SpringBean
    private transient CatalogService catalogService;

    static {
        CATEGORY_LIST = Collections.unmodifiableList(Arrays.asList("FISH", "DOGS", "REPTILES", "CATS", "BIRDS"));
        SEX_LIST = Collections.unmodifiableList(Arrays.asList("MALE","FEMALE"));
    }

    public List<String> getCategories() {
        return CATEGORY_LIST;
    }
    public List<String> getSex(){
        return SEX_LIST;
    }

    private AnimalMating animalMating;

    private Logger logger = LoggerFactory.getLogger(AnimalActionBean.class);

    private FileBean fileBean;

    private List<AnimalMating> animalMatingList;

    public static final int PAGESIZE = 10;
    private int id;
    private int cpage;
    private int psStr;
    private int pageCount;
    private int postCount;
    private int preBlock;
    private int nextBlock;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCpage() { return cpage; }
    public void setCpage(int cpage) { this.cpage = cpage; }

    public int getPsStr() { return psStr; }
    public void setPsStr(int psStr) { this.psStr = psStr; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public int getPreBlock() { return preBlock; }
    public void setPreBlock(int preBlock) { this.preBlock = preBlock; }

    public int getNextBlock() { return nextBlock; }
    public void setNextBlock(int nextBlock) { this.nextBlock = nextBlock; }

    public List<AnimalMating> getAnimalMatingList() { return animalMatingList; }
    public void setAnimalMatingList(List<AnimalMating> animalMatingList) { this.animalMatingList = animalMatingList; }

    public AnimalMating getAnimalMating() { return animalMating; }
    public void setAnimalMating(AnimalMating animalMating) { this.animalMating = animalMating; }

    public void setFileBean(FileBean fileBean) { this.fileBean = fileBean; }
    public FileBean getFileBean() { return fileBean; }

    private String keyword;


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getSearchOptionList() {
        return searchOptionList;
    }

    public void setSearchOption(String searchOption) {
        this.searchOption = searchOption;
    }



    @Autowired
    public AWSS3 awsS3 = AWSS3.getInstance();

    private String bucketName="jpet-img";

    // 파일 업로드 요청
    public Resolution uploadImg() throws Exception {
        HttpSession session = context.getRequest().getSession();
        AccountActionBean accountBean = (AccountActionBean) session.getAttribute("/actions/Account.action");
        String userId=accountBean.getUsername();
        if(fileBean==null){
            setMessage("PLEASE POST IMG FILE");
            return new ForwardResolution(ERROR);
        }
        else if(animalMating.getTitle()==null||animalMating.getCharacters()==null||animalMating.getContents()==null||animalMating.getSex()==null){
            setMessage("내용을 모두 입력해주세요");
            return new ForwardResolution(ERROR);
        }
        String url=animalService.uploadImgFile(fileBean);
        animalMating.setImgUrl(url);
        animalMating.setUserId(userId);
        animalService.insertAnimal(animalMating);

        int temp = getPagingEnd(1, searchOption);
        int start = getPagingStart(temp);
        animalMatingList = animalService.getAnimalMatingList(start, PAGESIZE);

        return new ForwardResolution(LIST_ANIMAL_MATING);
    }

    public Resolution addAnimalMatingView(){
        return new ForwardResolution(ADD_ANIMAL_MATING);
    }

    public Resolution listAnimalAccount(){

        cpage = 1;
        int temp = getPagingEnd(cpage, searchOption);
        int start = getPagingStart(temp);

        animalMatingList = animalService.getAnimalMatingList(start, PAGESIZE);


        return new ForwardResolution(LIST_ANIMAL_MATING);
    }

    public Resolution getMatingInfo() {
        System.out.println("id = " + id);
        animalService.plusViewCount(id);
        animalMating = animalService.getAnimalMattingDetail(id);
        System.out.println(animalMating.getSex());
        return new ForwardResolution(DETAIL_ANIMAL_MATING);
    }

    public Resolution paging() {
        System.out.println("cpage = " + cpage);
        int temp = getPagingEnd(cpage, searchOption);
        int start = getPagingStart(temp);
        animalMatingList = animalService.getAnimalMatingList(start, PAGESIZE);
        return new ForwardResolution(LIST_ANIMAL_MATING);
    }

    private int getPagingEnd(int cpage, String searchOption) {
        this.cpage = cpage;

        psStr = PAGESIZE;
        if(searchOption == null) {
            searchOption = "all";
        }

        postCount = animalService.getCount(searchOption);

        pageCount = (postCount - 1) / PAGESIZE + 1;
        if(cpage < 1)
            cpage = 1;
        else if(cpage > pageCount)
            cpage = pageCount;

        preBlock = (cpage -1)/PAGESIZE * PAGESIZE;
        nextBlock = preBlock + PAGESIZE + 1;

        return PAGESIZE* cpage;

    }

    private int getPagingStart(int end) {
        return end - PAGESIZE + 1;
    }


    public ForwardResolution searchMating() {
        int temp = getPagingEnd(cpage, searchOption);
        int start = getPagingStart(temp);
        if (keyword == null || keyword.length() < 1) {
            animalMatingList = animalService.getAnimalMatingList(start, PAGESIZE);
            return new ForwardResolution(LIST_ANIMAL_MATING);
        } else {
            if (searchOption.equals("Title")) {
                animalMatingList = animalService.searchAnimalMatingTitle(start, PAGESIZE, keyword);
                return new ForwardResolution(LIST_ANIMAL_MATING);
            } else if (searchOption.equals("Contents")) {
                animalMatingList = animalService.searchAnimalMatingContents(start, PAGESIZE, keyword);
                return new ForwardResolution(LIST_ANIMAL_MATING);
            } else if (searchOption.equals("UserName")) {
                animalMatingList = animalService.searchAnimalMatingUser(start, PAGESIZE, keyword);
                return new ForwardResolution(LIST_ANIMAL_MATING);
            }
            else {
                animalMatingList = animalService.getAnimalMatingList(start, PAGESIZE);
                return new ForwardResolution(LIST_ANIMAL_MATING);
            }
        }
    }

}
