export type VelogPost = {
  id: string;
  title: string;
  short_description: string;
  url_slug: string;
  tags: string[];
  released_at: string;
};

export type VelogUserProfile = {
  id: string;
  username: string;
  profile: {
    display_name: string;
    short_bio: string;
    thumbnail: string | null;
  };
  velog_config: {
    title: string | null;
    logo_image: string | null;
  };
};

export type VelogPostsResponse = {
  data?: {
    posts?: VelogPost[];
  };
  errors?: Array<{
    message: string;
  }>;
};

export type VelogUserResponse = {
  data?: {
    user?: VelogUserProfile;
  };
  errors?: Array<{
    message: string;
  }>;
};

export type VelogBlogProfile = {
  username: string;
  displayName: string;
  shortBio: string;
  thumbnail: string | null;
  title: string;
};
