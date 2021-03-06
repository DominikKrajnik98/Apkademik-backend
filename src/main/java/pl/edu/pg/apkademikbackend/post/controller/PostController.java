package pl.edu.pg.apkademikbackend.post.controller;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pg.apkademikbackend.WebSecurity.config.JwtTokenUtil;
import pl.edu.pg.apkademikbackend.noticeboard.exception.NoticeBoardNotFoundException;
import pl.edu.pg.apkademikbackend.post.PostService;
import pl.edu.pg.apkademikbackend.post.exception.PostNotFoundException;
import pl.edu.pg.apkademikbackend.post.exception.PostNotOnThisNoticeBoardException;
import pl.edu.pg.apkademikbackend.noticeboard.model.NoticeBoard;
import pl.edu.pg.apkademikbackend.post.model.Post;
import pl.edu.pg.apkademikbackend.noticeboard.repository.NoticeBoardRepository;
import pl.edu.pg.apkademikbackend.post.model.PostDto;
import pl.edu.pg.apkademikbackend.post.repository.PostRepository;
import pl.edu.pg.apkademikbackend.user.repositry.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;

    @Autowired
    NoticeBoardRepository noticeBoardRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/noticeBoard/{noticeBoard}/post")
    public ResponseEntity<?> addPost(@PathVariable String noticeBoard,@RequestBody Post newPost,HttpServletRequest request) throws Exception{
        LocalDateTime date=LocalDateTime.now();
        newPost.setDate(date);

        final String requestTokenHeader = request.getHeader("Authorization");

        String user = null;
        String jwtToken = null;

        jwtToken = requestTokenHeader.substring(7);
        try {
            user = jwtTokenUtil.getUsernameFromToken(jwtToken);
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to get JWT Token");
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Token has expired");
        }
        newPost.setUser(userRepository.findByEmail(user));


        NoticeBoard testNoticeBoard= noticeBoardRepository.findByName(noticeBoard);
        if(testNoticeBoard == null)
            throw new NoticeBoardNotFoundException(noticeBoard);

        newPost.setNoticeBoard(testNoticeBoard);

        return ResponseEntity.ok(postRepository.save(newPost));
    }

    @GetMapping("/noticeBoard/{noticeBoard}/posts")
    public ResponseEntity<?> getAllPosts(@PathVariable String noticeBoard)  throws Exception{
        NoticeBoard testNoticeBoard= noticeBoardRepository.findByName(noticeBoard);
        if(testNoticeBoard == null)
            throw new NoticeBoardNotFoundException(noticeBoard);


        Iterable<Post> posts=postRepository.findAllByDate(testNoticeBoard.getId());
        List<PostDto> postsData=new ArrayList<PostDto>();
        for (Post post:posts ) {
            PostDto newPostDto= returnPostData(post);
            postsData.add(newPostDto);
        }

        return ResponseEntity.ok(postsData);
    }

    @GetMapping("/noticeBoard/{noticeBoard}/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable long id,@PathVariable String noticeBoard,HttpServletRequest request)  throws Exception{

        NoticeBoard testNoticeBoard= noticeBoardRepository.findByName(noticeBoard);
        if(testNoticeBoard == null)
            throw new NoticeBoardNotFoundException(noticeBoard);

        Post post=postRepository.findById(id);
        if(post==null)
            throw new PostNotFoundException(id);


        if(post.getNoticeBoard().getId()!=testNoticeBoard.getId())
            throw new PostNotOnThisNoticeBoardException(id,noticeBoard);

        PostDto postDto=returnPostData(post);


        final String requestTokenHeader = request.getHeader("Authorization");

        String user = null;
        String jwtToken = null;

        jwtToken = requestTokenHeader.substring(7);
        try {
            user = jwtTokenUtil.getUsernameFromToken(jwtToken);
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to get JWT Token");
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Token has expired");
        }

        if(post.getUsers()==userRepository.findByEmail(user))
            postDto.setIsAuthor(true);
        return ResponseEntity.ok(postDto);
    }

    public PostDto returnPostData(Post post){
        PostDto postDto=new PostDto(post.getId(),post.getTitle(),post.getText(),post.getDate(),post.getUsers().getName(),post.getUsers().getSurname(),1);
        return postDto;
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<?>getPostById(@PathVariable long id){
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/post/{id}")
    public ResponseEntity<?>updatePostById(@PathVariable long id, @RequestBody Post post){
        return ResponseEntity.ok(postService.updatePostById(id,post));
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<?>deletePostById(@PathVariable long id){
        postService.deletePostById(id);
        return ResponseEntity.ok().build();
    }


}