package com.nutrymaco.orm.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrymaco.orm.records.MovieRecord;

import java.util.List;

public class MovieObjects {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String movie1String = """
                {
                "id" : 1,
                "name" : "Wolf from Wall-Street",
                "year" : 2018,
                "actors" : [
                    {
                        "id" : 1,
                        "name" : "Di Caprio",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO TOP ACTORS",
                            "city" : {
                                "id" : 1,
                                "count": 10000000,
                                "name" : "Los Angeles"
                            }
                        },
                        "city" : {
                            "id" : 1,
                            "count": 10,
                            "name" : "New-York"
                        }
                    },
                    {
                        "id" : 2,
                        "name" : "DJona Hill",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 1,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 1,
                            "count": 10,
                            "name" : "New-York"
                        }
                    }
                ]
                }
                """;
    private static final String movie2String = """
            {
                "id" : 2,
                "name" : "Moneyball",
                "year" : 2011,
                "actors" : [
                        {
                            "id" : 3,
                            "name" : "Bradd Pitt",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                    "id" : 3,
                                    "count": 10,
                                    "name" : "Oklahoma"
                                }
                        },
                        {
                            "id" : 2,
                            "name" : "DJona Hill",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO MIDDLE ACTORS",
                                "city" : {
                                    "id" : 2,
                                    "count": 10,
                                    "name" : "Los Santos"
                                }
                            },
                            "city" : {
                                    "id" : 1,
                                    "count": 10,
                                    "name" : "New-York"
                            }
                        }
                    ]
                }
            
            """;

    private static final String movie3String = """
            {
                "id" : 3,
                "name" : "Vice",
                "year" : 2018,
                "actors" : [
                        {
                            "id" : 4,
                            "name" : "Christian Bale",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                "id" : 4,
                                "name" : "Pembrukshir",
                                "count" : 5
                                }
                        },
                        {
                            "id" : 5,
                            "name" : "Steve Carell",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                "id" : 5,
                                "name" : "Conkord",
                                "count" : 4
                                }
                        }
                    ]
            }
            
            """;
    private static final String movie4String = """
            {
                "id" : 4,
                "name" : "The Big Short",
                "year" : 2015,
                "actors" : [
                        {
                            "id" : 4,
                            "name" : "Christian Bale",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                "id" : 4,
                                "name" : "Pembrukshir",
                                "count" : 5
                                }
                        },
                        {
                            "id" : 5,
                            "name" : "Steve Carell",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                "id" : 5,
                                "name" : "Conkord",
                                "count" : 4
                                }
                        },
                        {
                            "id" : 3,
                            "name" : "Bradd Pitt",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO TOP ACTORS",
                                "city" : {
                                    "id" : 1,
                                    "count": 10000000,
                                    "name" : "Los Angeles"
                                }
                            },
                            "city" : {
                                    "id" : 3,
                                    "count": 10,
                                    "name" : "Oklahoma"
                                }
                        },
                        {
                            "id" : 6,
                            "name" : "Rafe Spall",
                            "organisation" : {
                                "id" : 1,
                                "name" : "OOO MIDDLE ACTORS",
                                "city" : {
                                    "id" : 2,
                                    "count": 10,
                                    "name" : "Los Santos"
                                }
                            },
                            "city" : {
                                    "id" : 3,
                                    "count": 10,
                                    "name" : "Oklahoma"
                                }
                        }
                ]
            }
            """;

    private static final String movie5String = """
            {
                "id" : 5,
                "name" : "Blade Runner 2049",
                "year" : 2017,
                "actors" : [
                    {
                        "id" : 7,
                        "name" : "Ryan Gosling",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 2,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 6,
                            "name" : "London",
                            "count" : 12
                        }
                    },
                    {
                        "id" : 8,
                        "name" : "Harrison Ford",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 2,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 7,
                            "name" : "Chicago",
                            "count" : 7
                        }
                    },
                    {
                        "id" : 9,
                        "name" : "Ana de Armas",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 2,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 8,
                            "name" : "Gavana",
                            "count" : 3
                        }
                    }
                ]
            }
            """;
    private static final String movie6String = """
            {
                "id" : 6,
                "name" : "Blade Runner",
                "year" : 1982,
                "actors" : [
                    {
                        "id" : 7,
                        "name" : "Ryan Gosling",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 2,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 6,
                            "name" : "London",
                            "count" : 12
                        }
                    },
                    {
                        "id" : 8,
                        "name" : "Harrison Ford",
                        "organisation" : {
                            "id" : 1,
                            "name" : "OOO MIDDLE ACTORS",
                            "city" : {
                                "id" : 2,
                                "count": 10,
                                "name" : "Los Santos"
                            }
                        },
                        "city" : {
                            "id" : 7,
                            "name" : "Chicago",
                            "count" : 7
                        }
                    }
                ]
            }
            """;

    public static MovieRecord wolfFromWallStreet;
    public static MovieRecord moneyball;
    public static MovieRecord vice;
    public static MovieRecord theBigShort;
    public static MovieRecord bladeRunner2049;
    public static MovieRecord bladeRunner;
    public static List<MovieRecord> movies;

    static {
        try {
            wolfFromWallStreet = mapper.readValue(movie1String, MovieRecord.class);
            moneyball = mapper.readValue(movie2String, MovieRecord.class);
            vice = mapper.readValue(movie3String, MovieRecord.class);
            theBigShort = mapper.readValue(movie4String, MovieRecord.class);
            bladeRunner2049 = mapper.readValue(movie5String, MovieRecord.class);
            bladeRunner = mapper.readValue(movie6String, MovieRecord.class);
            movies = List.of(wolfFromWallStreet, moneyball, vice, theBigShort, bladeRunner,
                    bladeRunner2049);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
